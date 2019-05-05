package io.github.linktosriram.deck.controllers

import io.github.linktosriram.deck.CfClient
import io.github.linktosriram.deck.aggregators.ServiceInstancesAggregator
import io.github.linktosriram.deck.domain.cf.CfSpace
import io.github.linktosriram.deck.domain.view.AppListing
import io.github.linktosriram.deck.domain.view.ServiceInstanceListing
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.util.function.component1
import reactor.util.function.component2

@RestController
@RequestMapping("/api/v1/spaces")
class SpaceController(
    private val scheduler: Scheduler,
    private val cfClient: CfClient,
    private val instancesAggregator: ServiceInstancesAggregator) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun listSpaces(@RequestParam orgGuid: String): Flux<CfSpace> {
        return cfClient.listSpaces(orgGuid)
    }

    @GetMapping(value = ["/{spaceGuid}/apps"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun appListPage(@PathVariable spaceGuid: String): Mono<AppListing> {
        val spaceName = cfClient.getSpace(spaceGuid)
            .map { it.entity.name }
            .subscribeOn(scheduler)

        val apps = cfClient.listApps(spaceGuid)
            .sort(compareBy { it.name.toLowerCase() })
            .collectList()
            .subscribeOn(scheduler)

        return Mono.zip(spaceName, apps)
            .map { (spaceName, apps) ->
                AppListing(spaceName, apps)
            }
    }

    @GetMapping(value = ["/{spaceGuid}/service-instances"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun serviceInstanceList(@PathVariable spaceGuid: String): Mono<ServiceInstanceListing> {
        val spaceName = cfClient.getSpace(spaceGuid)
            .map { it.entity.name }
            .subscribeOn(scheduler)

        val serviceInstances = instancesAggregator.serviceInstances(spaceGuid)
            .sort(compareBy { it.name.toLowerCase() })
            .collectList()
            .subscribeOn(scheduler)

        return Mono.zip(spaceName, serviceInstances)
            .map { (spaceName, serviceInstances) ->
                ServiceInstanceListing(spaceName, serviceInstances)
            }
    }
}
