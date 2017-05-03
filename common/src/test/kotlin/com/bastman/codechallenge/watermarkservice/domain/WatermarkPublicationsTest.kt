package com.bastman.codechallenge.watermarkservice.domain

import junit.framework.AssertionFailedError
import org.amshove.kluent.`should equal to`
import org.junit.jupiter.api.Test


internal class WatermarkPublicationsTest {

    private val methodUnderTest = ::watermarkPublication

    @Test
    fun watermarkJournalTest() {
        val sourcePublication = SourcePublication.Journal(
                title = "title-of-journal",
                author = "author-of-journal",
                content = "content-of-journal"
        )

        testJournal(sourcePublication)
    }

    @Test
    fun watermarkBookMediaTest() {
        val sourcePublication = SourcePublication.Book(
                title = "title-of-book-media",
                author = "author-of-book-media",
                content = "content-book-media",
                topic = BookTopic.Media
        )

        testBook(sourcePublication)
    }

    @Test
    fun watermarkBookScienceTest() {
        val sourcePublication = SourcePublication.Book(
                title = "title-of-book-science",
                author = "author-of-book-science",
                content = "content-book-science",
                topic = BookTopic.Science
        )

        testBook(sourcePublication)
    }

    @Test
    fun watermarkBookBusinessTest() {
        val sourcePublication = SourcePublication.Book(
                title = "title-of-book-business",
                author = "author-of-book-business",
                content = "content-book-business",
                topic = BookTopic.Business
        )

        testBook(sourcePublication)
    }

    private fun testJournal(sourcePublication: SourcePublication.Journal) {
        val watermarkedPublication = watermarkJournal(sourcePublication)

        watermarkedPublication.title `should equal to` sourcePublication.title
        watermarkedPublication.author `should equal to` sourcePublication.author
        watermarkedPublication.content `should equal to` sourcePublication.content

        val watermark = watermarkedPublication.watermark

        watermark.title `should equal to` sourcePublication.title
        watermark.author `should equal to` sourcePublication.author
        watermark.content `should equal to` sourcePublication.content
    }

    private fun testBook(sourcePublication: SourcePublication.Book) {
        val watermarkedPublication = watermarkBook(sourcePublication)

        watermarkedPublication.title `should equal to` sourcePublication.title
        watermarkedPublication.author `should equal to` sourcePublication.author
        watermarkedPublication.content `should equal to` sourcePublication.content
        watermarkedPublication.topic.name `should equal to` sourcePublication.topic.name

        val watermark = watermarkedPublication.watermark

        watermark.title `should equal to` sourcePublication.title
        watermark.author `should equal to` sourcePublication.author
        watermark.content `should equal to` sourcePublication.content
        watermark.topic.name `should equal to` sourcePublication.topic.name
    }


    private fun watermarkJournal(sourcePublication: SourcePublication.Journal): WatermarkedPublication.Journal {
        val watermarkedPublication = methodUnderTest(sourcePublication)
        return when (watermarkedPublication) {
            is WatermarkedPublication.Journal -> watermarkedPublication
            else -> throw AssertionFailedError("Expected result of type ${WatermarkedPublication.Journal::class}")
        }
    }

    private fun watermarkBook(sourcePublication: SourcePublication.Book): WatermarkedPublication.Book {
        val watermarkedPublication = methodUnderTest(sourcePublication)
        return when (watermarkedPublication) {
            is WatermarkedPublication.Book -> watermarkedPublication
            else -> throw AssertionFailedError("Expected result of type ${WatermarkedPublication.Book::class}")
        }
    }
}