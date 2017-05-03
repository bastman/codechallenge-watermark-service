package com.bastman.codechallenge.watermarkservice.restservice.domain.pipeline

import com.bastman.codechallenge.watermarkservice.domain.SourcePublication
import com.bastman.codechallenge.watermarkservice.domain.WatermarkedPublication
import com.bastman.codechallenge.watermarkservice.domain.watermarkPublication
import com.bastman.codechallenge.watermarkservice.logging.AppLogger
import com.bastman.codechallenge.watermarkservice.restservice.domain.repository.PendingPublicationsRepository
import com.bastman.codechallenge.watermarkservice.restservice.domain.repository.WatermarkedPublicationsRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

data class SourceMessage(
        val ticketId: String,
        val publication: SourcePublication
)

data class SinkMessage(
        val ticketId: String,
        val watermarkedPublication: WatermarkedPublication
)

typealias WatermarkedPublicationSupplier = (SourcePublication) -> WatermarkedPublication
typealias WatermarkJobTicketIdSupplier = () -> String

class Pipeline(
        val sourceChannel: Channel<SourceMessage> = Channel(10_000),
        val watermarkedPublicationsChannel: Channel<SinkMessage> = Channel(10_000),
        val watermarkedPublicationSupplier: WatermarkedPublicationSupplier = ::watermarkPublication,
        val watermarkJobTicketIdSupplier: WatermarkJobTicketIdSupplier = {
            "${Instant.now().toEpochMilli()}-${UUID.randomUUID().toString()}"
        },
        val watermarkedPublicationsRepository: WatermarkedPublicationsRepository,
        val pendingPublicationsRepository: PendingPublicationsRepository
) {

    private val LOGGER = AppLogger.get(javaClass)

    object ConsumerContext {
        val SOURCE = CommonPool
        val WATERMARK = newFixedThreadPoolContext(nThreads = 4, name = "watermark-pipeline-watermark-job")
    }

    private val sourceChannelConsumer = launch(context = ConsumerContext.SOURCE, start = false) {
        while (isActive) {
            sourceChannel.consumeEach { watermark(sourceMessage = it) }
        }
    }

    private val watermarkedChannelConsumer = launch(context = ConsumerContext.WATERMARK, start = false) {
        while (isActive) {
            watermarkedPublicationsChannel.consumeEach { publishWatermarkedMessageToRepository(sinkMessage = it) }
        }
    }

    fun startWorking() {
        LOGGER.info("==== start working ===")
        sourceChannelConsumer.start()
        watermarkedChannelConsumer.start()
    }

    suspend fun submitJob(sourceMessage: SourceMessage) {
        publishPendingMessageToRepository(sourceMessage = sourceMessage)
        sourceChannel.send(sourceMessage)
    }


    private suspend fun watermark(sourceMessage: SourceMessage) {
        LOGGER.info("start processing ticketId=${sourceMessage.ticketId}")

        // simulate massive processing time
        delay(1, TimeUnit.SECONDS)

        val watermarkedPublication = watermarkedPublicationSupplier(sourceMessage.publication)

        LOGGER.info("finished processing ticketId=${sourceMessage.ticketId}")

        val sinkMessage = SinkMessage(
                ticketId = sourceMessage.ticketId,
                watermarkedPublication = watermarkedPublication
        )

        watermarkedPublicationsChannel.send(sinkMessage)
    }

    private suspend fun publishPendingMessageToRepository(sourceMessage: SourceMessage) {
        pendingPublicationsRepository.add(item = sourceMessage)

        LOGGER.info("watermark job ticket can be tracked now. ticketId=${sourceMessage.ticketId}")
    }

    private suspend fun publishWatermarkedMessageToRepository(sinkMessage: SinkMessage) {
        watermarkedPublicationsRepository.add(item = sinkMessage)

        LOGGER.info("watermarked publication is now available for download. ticketId=${sinkMessage.ticketId}")
    }

}

fun Pipeline.generateWatermarkJobTicketId() = watermarkJobTicketIdSupplier()
