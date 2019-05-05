package io.github.linktosriram.deck.controllers

import io.github.linktosriram.deck.CfClient
import io.github.linktosriram.deck.aggregators.AppOverviewAggregator
import io.github.linktosriram.deck.aggregators.PaginatedEventsAggregator
import io.github.linktosriram.deck.aggregators.ServiceBindingsAggregator
import io.github.linktosriram.deck.domain.view.AppOverviewPage
import io.github.linktosriram.deck.domain.view.EnvironmentPage
import io.github.linktosriram.deck.domain.view.EventsPage
import io.github.linktosriram.deck.domain.view.ServiceBindingsPage
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.util.function.component1
import reactor.util.function.component2

@RestController
@RequestMapping("/api/v1/apps")
class AppController(
    private val scheduler: Scheduler,
    private val cfClient: CfClient,
    private val overviewAggregator: AppOverviewAggregator,
    private val bindingsAggregator: ServiceBindingsAggregator,
    private val eventsAggregator: PaginatedEventsAggregator) {

    @GetMapping(value = ["/{appGuid}"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun overview(@PathVariable appGuid: String): Mono<AppOverviewPage> {
        return overviewAggregator.getOverview(appGuid)
    }

    @GetMapping(value = ["/{appGuid}/service-bindings"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun serviceBindings(@PathVariable appGuid: String): Mono<ServiceBindingsPage> {
        val appName = cfClient.getApp(appGuid)
            .map { it.entity.name }
            .subscribeOn(scheduler)

        val bindings = bindingsAggregator.getServiceBindings(appGuid)
            .sort(compareBy { it.name.toLowerCase() })
            .collectList()
            .subscribeOn(scheduler)

        return Mono.zip(appName, bindings)
            .map { (appName, bindings) ->
                ServiceBindingsPage(appName = appName, bindings = bindings)
            }
    }

    @GetMapping(value = ["/{appGuid}/env"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun env(@PathVariable appGuid: String): Mono<EnvironmentPage> {
        val appName = cfClient.getApp(appGuid)
            .map { it.entity.name }
            .subscribeOn(scheduler)

        val env = cfClient.getEnv(appGuid)
            .subscribeOn(scheduler)

        return Mono.zip(appName, env)
            .map { (appName, env) ->
                EnvironmentPage(appName = appName, env = env)
            }
    }

    @GetMapping(value = ["/{appGuid}/events"])
    fun events(@PathVariable appGuid: String, @RequestParam page: Int): Mono<EventsPage> {
        val appName = cfClient.getApp(appGuid)
            .map { it.entity.name }
            .subscribeOn(scheduler)

        val events = eventsAggregator.getAppEvents(appGuid, page)
            .subscribeOn(scheduler)

        return Mono.zip(appName, events)
            .map { (appName, events) ->
                EventsPage(appName = appName, events = events)
            }
    }
}
