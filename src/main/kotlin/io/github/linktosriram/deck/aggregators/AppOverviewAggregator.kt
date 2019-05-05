package io.github.linktosriram.deck.aggregators

import io.github.linktosriram.deck.CfClient
import io.github.linktosriram.deck.domain.view.AppOverviewPage
import io.github.linktosriram.deck.mappers.EventMetadataMapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.core.scheduler.Scheduler
import reactor.util.function.component1
import reactor.util.function.component2
import reactor.util.function.component3

@Service
class AppOverviewAggregator(
    private val scheduler: Scheduler,
    private val cfClient: CfClient,
    private val eventMapper: EventMetadataMapper) {

    fun getOverview(appGuid: String): Mono<AppOverviewPage> {
        val summary = cfClient.getAppSummary(appGuid).subscribeOn(scheduler)
        val stats = cfClient.getInstanceDetails(appGuid).collectList().subscribeOn(scheduler)
        val events = cfClient.recentEvents(appGuid)
            .map { eventMapper.map(it) }
            .collectList()
            .subscribeOn(scheduler)

        return Mono.zip(summary, stats, events)
            .flatMap { (summary, stats, events) ->
                AppOverviewPage(summary, stats, events).toMono()
            }
    }
}
