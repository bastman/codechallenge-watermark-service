package com.bastman.codechallenge.watermarkservice.restservice.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

object SpringConstants {
    const val BASE_PACKAGE = "com.bastman.codechallenge.watermarkservice.restservice"
    const val API_ROUTE_SWAGGER_UI = "/swagger-ui.html"
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
