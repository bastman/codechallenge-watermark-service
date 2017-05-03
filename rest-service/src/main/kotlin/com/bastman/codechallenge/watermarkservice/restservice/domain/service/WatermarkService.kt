package com.bastman.codechallenge.watermarkservice.restservice.domain.service

import com.bastman.codechallenge.watermarkservice.domain.SourcePublication
import com.bastman.codechallenge.watermarkservice.domain.WatermarkedPublication
import com.bastman.codechallenge.watermarkservice.restservice.domain.pipeline.Pipeline
import com.bastman.codechallenge.watermarkservice.restservice.domain.pipeline.SourceMessage
import com.bastman.codechallenge.watermarkservice.restservice.domain.pipeline.generateWatermarkJobTicketId
import com.bastman.codechallenge.watermarkservice.restservice.domain.repository.PendingPublicationsRepository
import com.bastman.codechallenge.watermarkservice.restservice.domain.repository.WatermarkedPublicationsRepository
import org.springframework.stereotype.Component

@Component
class WatermarkService(
        private val pendingPublicationsRepository: PendingPublicationsRepository,
        private val watermarkedPublicationsRepository: WatermarkedPublicationsRepository
) {
    enum class JobStatus() {
        PENDING,
        COMPLETE,
        NOT_FOUND
    }

    private val pipeline = Pipeline(
            watermarkedPublicationsRepository = watermarkedPublicationsRepository,
            pendingPublicationsRepository = pendingPublicationsRepository
    )

    suspend fun submitJob(sourceMessage: SourceMessage) = pipeline.submitJob(sourceMessage)
    fun generateWatermarkJobTicketId() = pipeline.generateWatermarkJobTicketId()
    fun startWorking() = pipeline.startWorking()

    fun describeJobStatus(ticketId: String): JobStatus {
        val watermarkedPublication = getWatermarkedPublication(ticketId = ticketId)
        watermarkedPublication?.let { return JobStatus.COMPLETE }

        val pendingPublication = getPendingPublication(ticketId = ticketId)
        pendingPublication?.let { return JobStatus.PENDING }

        return JobStatus.NOT_FOUND
    }

    fun getWatermarkedPublication(ticketId: String): WatermarkedPublication? = watermarkedPublicationsRepository
            .getOrNull(itemId = ticketId)?.watermarkedPublication

    private fun getPendingPublication(ticketId: String): SourcePublication? = pendingPublicationsRepository
            .getOrNull(itemId = ticketId)?.publication

}
