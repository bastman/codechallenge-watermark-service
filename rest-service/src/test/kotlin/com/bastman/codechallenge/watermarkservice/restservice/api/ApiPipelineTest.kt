package com.bastman.codechallenge.watermarkservice.restservice.api

import com.bastman.codechallenge.watermarkservice.restservice.RestServiceApplication
import com.bastman.codechallenge.watermarkservice.restservice.api.requesthandler.job.DescribeJobStatusRequestHandler
import com.bastman.codechallenge.watermarkservice.restservice.domain.service.WatermarkService
import com.bastman.codechallenge.watermarkservice.restservice.testutils.JsonCodec
import com.bastman.codechallenge.watermarkservice.restservice.testutils.JsonSchemaValidator
import com.bastman.codechallenge.watermarkservice.restservice.testutils.Junit5DynamicTestFactoryExtension
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.future.future
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldBeIn
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@ExtendWith(SpringExtension::class)
@SpringBootTest(
        classes = arrayOf(RestServiceApplication::class),
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Tag("fast")
internal class ApiPipelineTest : Junit5DynamicTestFactoryExtension {

    @TestFactory
    fun apiTests() = apiTests.toList()

    @Autowired
    lateinit var mockMvc: MockMvc

    private val apiTests: MutableList<DynamicTest> = mutableListOf()

    private data class TestCase(
            val requestResource: String,
            val responseResource: String,
            val downloadApiResponseSchemaValidator: JsonSchemaValidator
    )

    private val JSON = JsonCodec()

    private val statusApiResponseSchemaValidator = JsonSchemaValidator.of(
            schemaNode = JSON.decode(JSON.loadResourceAsText("/api/status/response-schema.json"))
    )

    private val downloadBookApiResponseSchemaValidator = JsonSchemaValidator.of(
            schemaNode = JSON.decode(JSON.loadResourceAsText("/api/download/book-response-schema.json"))
    )
    private val downloadJournalApiResponseSchemaValidator = JsonSchemaValidator.of(
            schemaNode = JSON.decode(JSON.loadResourceAsText("/api/download/journal-response-schema.json"))
    )


    @PostConstruct
    fun setupSpec() {
        Assertions.assertThat(mockMvc).isNotNull()

        registerTests()
    }

    private fun registerTests() {
        val testCases: List<TestCase> = listOf(
                TestCase(
                        requestResource = "/api/pipeline/source/journal-request.json",
                        responseResource = "/api/pipeline/sink/journal-response.json",
                        downloadApiResponseSchemaValidator = downloadJournalApiResponseSchemaValidator
                ),
                TestCase(
                        requestResource = "/api/pipeline/source/book-business-request.json",
                        responseResource = "/api/pipeline/sink/book-business-response.json",
                        downloadApiResponseSchemaValidator = downloadBookApiResponseSchemaValidator
                ),
                TestCase(
                        requestResource = "/api/pipeline/source/book-media-request.json",
                        responseResource = "/api/pipeline/sink/book-media-response.json",
                        downloadApiResponseSchemaValidator = downloadBookApiResponseSchemaValidator
                ),
                TestCase(
                        requestResource = "/api/pipeline/source/book-science-request.json",
                        responseResource = "/api/pipeline/sink/book-science-response.json",
                        downloadApiResponseSchemaValidator = downloadBookApiResponseSchemaValidator
                )
        )

        testCases.forEach { addTestCase(it) }
    }

    private fun addTestCase(testCase: TestCase) {
        apiTests.registerTest(
                "test api's (submit->status->download): with requestBody provided by=${testCase.requestResource}", {

            val submitApiResponse = submitApi(testCase = testCase)
            val ticketId = submitApiResponse.ticketId
            statusApi(ticketId = ticketId,
                    expectJobStatus = listOf(
                            WatermarkService.JobStatus.PENDING,
                            WatermarkService.JobStatus.COMPLETE
                    )
            )

            future {
                delay(3, TimeUnit.SECONDS)
                statusApi(
                        ticketId = ticketId,
                        expectJobStatus = listOf(
                                WatermarkService.JobStatus.COMPLETE
                        )
                )
            }.get()

            downloadApi(ticketId = ticketId, testCase = testCase)

        }
        )
    }

    private data class SubmitApiResponse(val ticketId: String)

    private fun submitApi(testCase: TestCase): SubmitApiResponse {

        val requestBody: String = javaClass.getResource(testCase.requestResource).readText()

        val mockMvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post(WatermarkApiController.ApiRoutes.PUBLICATION_SUBMIT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody)
        )
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

        val responseBody = mockMvcResult.response.contentAsString

        return JSON.decode(responseBody)
    }

    private fun statusApi(ticketId: String, expectJobStatus: List<WatermarkService.JobStatus>): WatermarkService.JobStatus {
        val route = WatermarkApiController.ApiRoutes.DESCRIBE_JOB_STATUS.replace(
                "{ticketId}", ticketId
        )


        val mockMvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get(route)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

        val responseBody = mockMvcResult.response.contentAsString

        val schemaValidationResult = statusApiResponseSchemaValidator.verify(
                content = JSON.decode(responseBody), deepCheck = true
        )

        schemaValidationResult.errors `should equal` emptyList()

        val response: DescribeJobStatusRequestHandler.Response = JSON.decode(responseBody)

        response.jobStatus shouldBeIn expectJobStatus

        return response.jobStatus
    }

    private fun downloadApi(ticketId: String, testCase: TestCase) {
        val route = WatermarkApiController.ApiRoutes.DOWNLOAD_WATERMARKED_PUBLICATION.replace(
                "{ticketId}", ticketId
        )

        val mockMvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get(route)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

        val responseBody = mockMvcResult.response.contentAsString

        val schemaValidationResult = testCase.downloadApiResponseSchemaValidator.verify(
                content = JSON.decode(responseBody), deepCheck = true
        )

        schemaValidationResult.errors `should equal` emptyList()

        val responseBodyGiven = JSON.normalize(responseBody)
        val responseBodyExpected = JSON.normalize(
                JSON.loadResourceAsText(testCase.responseResource)
        )

        responseBodyGiven `should equal` responseBodyExpected
    }


}