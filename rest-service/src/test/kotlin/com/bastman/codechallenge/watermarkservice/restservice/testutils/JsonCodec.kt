package com.bastman.codechallenge.watermarkservice.restservice.testutils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JsonCodec(private val objectMapper: ObjectMapper = jacksonObjectMapper()) {
    fun <T> decode(json: String, valueType: Class<T>): T = objectMapper.readValue(json, valueType)
    fun <T> decode(json: String, valueTypeRef: TypeReference<T>): T = objectMapper.readValue(json, valueTypeRef)
    inline fun <reified T> decode(content: String): T {
        return decode(content, object : TypeReference<T>() {})
    }

    fun decodeTree(content: String): JsonNode = objectMapper.readTree(content)

    fun encode(value: Any?) = objectMapper.writeValueAsString(value)

    fun loadResourceAsText(resource: String) = javaClass.getResource(resource).readText()

    fun normalize(content: String): String {
        val node: JsonNode? = decode(content)

        return encode(node).trim()
    }

}
