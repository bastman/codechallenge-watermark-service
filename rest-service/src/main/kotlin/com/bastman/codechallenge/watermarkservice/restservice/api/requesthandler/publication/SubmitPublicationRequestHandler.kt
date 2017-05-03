package com.bastman.codechallenge.watermarkservice.restservice.api.requesthandler.publication

import com.bastman.codechallenge.watermarkservice.domain.BookTopic
import com.bastman.codechallenge.watermarkservice.domain.SourcePublication
import com.bastman.codechallenge.watermarkservice.restservice.domain.pipeline.SourceMessage
import com.bastman.codechallenge.watermarkservice.restservice.domain.service.WatermarkService
import kotlinx.coroutines.experimental.future.future
import org.springframework.stereotype.Component

@Component
class SubmitPublicationRequestHandler(private val watermarkService: WatermarkService) {

    data class Request(val content: String, val title: String, val author: String, val topic: BookTopic?)
    data class Response(val ticketId: String)

    fun handleRequest(request: Request): Response = future {
        handleRequestAsync(request)
    }.get()

    private suspend fun handleRequestAsync(request: Request): Response {
        val pipelineMessage = SourceMessage(
                ticketId = generateTicketId(),
                publication = request.toPublication()
        )
        watermarkService.submitJob(pipelineMessage)

        return Response(ticketId = pipelineMessage.ticketId)
    }

    private fun generateTicketId() = watermarkService.generateWatermarkJobTicketId()
}

fun SubmitPublicationRequestHandler.Request.toPublication() = when (topic) {
    null -> SourcePublication.Journal(
            title = title,
            author = author,
            content = content
    )
    else -> SourcePublication.Book(
            title = title,
            author = author,
            content = content,
            topic = topic
    )
}