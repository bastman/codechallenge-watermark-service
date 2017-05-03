package com.bastman.codechallenge.watermarkservice.restservice.domain.repository

import com.bastman.codechallenge.watermarkservice.logging.AppLogger
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit
import com.bastman.codechallenge.watermarkservice.restservice.domain.pipeline.SourceMessage as RepositoryItem

typealias PendingPublicationsCache = com.github.benmanes.caffeine.cache.Cache<String, RepositoryItem>
@Component
class PendingPublicationsRepository {
    private val LOGGER = AppLogger.get(javaClass)

    private val cache: PendingPublicationsCache by lazy {
        val expiry = Duration.ofDays(3)

        Caffeine
                .newBuilder()
                .maximumSize(1_000_000)
                .expireAfterWrite(expiry.seconds, TimeUnit.SECONDS)
                .build<String, RepositoryItem>()
    }

    fun add(item: RepositoryItem) {
        val itemId = supplyItemId(item)
        cache.put(itemId, item)

        LOGGER.info("add item to repository. itemId=$itemId")
    }

    fun getOrNull(itemId: String) = cache.getIfPresent(itemId)
}

fun PendingPublicationsRepository.supplyItemId(item: RepositoryItem) = item.ticketId
