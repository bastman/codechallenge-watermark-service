package com.bastman.codechallenge.watermarkservice.restservice.testutils

import org.junit.jupiter.api.DynamicTest

interface Junit5DynamicTestFactoryExtension {

    fun MutableList<DynamicTest>.registerTest(name: String, test: () -> Unit): DynamicTest {
        val dynamicTest = createTest(name, test)
        this.add(dynamicTest)

        return dynamicTest
    }

    fun List<DynamicTest>.createTest(name: String, test: () -> Unit): DynamicTest {

        return DynamicTest.dynamicTest(name, test)
    }
}

