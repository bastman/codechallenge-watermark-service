package com.bastman.codechallenge.watermarkservice.restservice.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock


object SpringConstants {

    const val BASE_PACKAGE = "com.bastman.codechallenge.watermarkservice.restservice"

    const val API_ROUTE_SWAGGER_UI = "/swagger-ui.html"
    const val SWAGGER_UI_WEBJARS_ANT_MATCHER = "/webjars/springfox-swagger-ui/**"
    const val SWAGGER_UI_RESOURCES_ANT_MATCHER = "/swagger-resources/**"

    val SWAGGER_UI_ANT_MATCHERS: List<String> = listOf(
            API_ROUTE_SWAGGER_UI,
            SWAGGER_UI_WEBJARS_ANT_MATCHER,
            SWAGGER_UI_RESOURCES_ANT_MATCHER,
            "/swagger*/**",
            "/v2/**",
            "/webjars/.*",
            "/v2/api-docs.*"
    )


    const val API_ROUTE_FOO_LIST = "/api/foo"
}

@Configuration
open class ClockConfiguration {
    @Bean
    open fun clock(): Clock = Clock.systemUTC()
}

@Configuration
open class JsonMapperConfiguration {

    companion object {
        val DEFAULT_MAPPER = jacksonObjectMapper()
    }

    @Bean
    open fun objectMapper(): ObjectMapper = DEFAULT_MAPPER
}
