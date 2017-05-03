package com.bastman.codechallenge.watermarkservice.restservice.api

import com.bastman.codechallenge.watermarkservice.restservice.RestServiceApplication
import com.bastman.codechallenge.watermarkservice.restservice.testutils.JsonCodec
import com.bastman.codechallenge.watermarkservice.restservice.testutils.JsonSchemaValidator
import com.bastman.codechallenge.watermarkservice.restservice.testutils.Junit5DynamicTestFactoryExtension
import com.fasterxml.jackson.databind.JsonNode
import org.amshove.kluent.`should equal`
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
import javax.annotation.PostConstruct

@ExtendWith(SpringExtension::class)
@SpringBootTest(
        classes = arrayOf(RestServiceApplication::class),
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Tag("fast")
internal class PublicationSubmitApiTest : Junit5DynamicTestFactoryExtension {

    @TestFactory
    fun apiTests() = apiTests.toList()

    @Autowired
    lateinit var mockMvc: MockMvc

    private val apiEndpointUnderTest = WatermarkApiController.ApiRoutes.PUBLICATION_SUBMIT

    private val givenTestResourceBasePath = "/api/submit"
    private val givenResponseSchemaLocation = "$givenTestResourceBasePath/response-schema.json"

    private fun registerTests() {
        val testCases: List<TestCase> = listOf(
                TestCase(requestResource = "$givenTestResourceBasePath/journal-request.json"),
                TestCase(requestResource = "$givenTestResourceBasePath/book-business-request.json"),
                TestCase(requestResource = "$givenTestResourceBasePath/book-media-request.json"),
                TestCase(requestResource = "$givenTestResourceBasePath/book-science-request.json")
        )

        testCases.forEach {
            apiTests.registerTest(
                    "test: $apiEndpointUnderTest with requestBody provided by=${it.requestResource}", { doTest(it) }
            )
        }

    }

    private val JSON = JsonCodec()
    private val givenResponseSchema: JsonNode = JSON.decode(
            javaClass.getResource(givenResponseSchemaLocation).readText()
    )
    private val responseSchemaValidator = JsonSchemaValidator.of(givenResponseSchema)

    private val apiTests: MutableList<DynamicTest> = mutableListOf()

    private data class TestCase(val requestResource: String)

    @PostConstruct
    fun setupSpec() {
        Assertions.assertThat(mockMvc).isNotNull()

        registerTests()
    }

    private fun doTest(testCase: TestCase) {

        val requestBody: String = javaClass.getResource(testCase.requestResource).readText()

        val mockMvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post(apiEndpointUnderTest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody)
        )
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))//
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()


        val responseBody = mockMvcResult.response.contentAsString

        val schemaValidationResult = responseSchemaValidator.verify(
                content = JSON.decode(responseBody), deepCheck = true
        )

        schemaValidationResult.errors `should equal` emptyList()
    }

}
