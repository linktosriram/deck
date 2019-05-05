package io.github.linktosriram.deck.aggregators

import io.github.linktosriram.deck.CfClient
import io.github.linktosriram.deck.domain.view.PaginatedEventView
import io.github.linktosriram.deck.mappers.EventMetadataMapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

@Service
class PaginatedEventsAggregator(
    private val scheduler: Scheduler,
    private val cfClient: CfClient,
    private val eventMapper: EventMetadataMapper) {

    fun getAppEvents(appGuid: String, page: Int): Mono<PaginatedEventView> {
        return cfClient.appEvents(appGuid, page)
            .map { pEvents ->
                PaginatedEventView(
                    totalResults = pEvents.totalResults,
                    resources = pEvents.resources.map { eventMapper.map(it) })
            }
            .subscribeOn(scheduler)
    }
}
