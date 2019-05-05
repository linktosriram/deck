package io.github.linktosriram.deck.aggregators

import io.github.linktosriram.deck.CfClient
import io.github.linktosriram.deck.domain.cf.ApplicationDetail
import io.github.linktosriram.deck.domain.cf.PaginatedServiceBindings
import io.github.linktosriram.deck.domain.cf.PaginatedServiceKeys
import io.github.linktosriram.deck.domain.cf.ServiceInstance
import io.github.linktosriram.deck.domain.view.ServiceInstanceRow
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.core.scheduler.Scheduler
import reactor.util.function.component1
import reactor.util.function.component2
import reactor.util.function.component3
import reactor.util.function.component4
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Service
class ServiceInstancesAggregator(
    private val scheduler: Scheduler,
    private val cfClient: CfClient) {

    private val labelCache: ConcurrentMap<String, String> = ConcurrentHashMap()
    private val planNameCache: ConcurrentMap<String, String> = ConcurrentHashMap()
    private val appCache: ConcurrentMap<String, ApplicationDetail> = ConcurrentHashMap()

    fun serviceInstances(spaceGuid: String): Flux<ServiceInstanceRow> {
        return cfClient.listServiceInstances(spaceGuid)
            .parallel()
            .runOn(scheduler)
            .flatMap { getRow(it) }
            .sequential()
            .subscribeOn(scheduler)
    }

    private fun getRow(serviceInstance: ServiceInstance): Mono<ServiceInstanceRow> {
        val entity = serviceInstance.entity

        val serviceLabel = getServiceLabel(entity.serviceGuid)
        val servicePlanName = getServicePlan(entity.servicePlanGuid)
        val serviceKeysCount = getServiceKeysCount(entity.type, entity.serviceKeysUrl)
        val referencingApps = getReferencingApps(entity.serviceBindingsUrl)
            .sort(compareBy { it.entity.name.toLowerCase() })
            .collectList()

        return Mono.zip(serviceLabel, servicePlanName, serviceKeysCount, referencingApps)
            .map { (serviceLabel, servicePlanName, serviceKeysCount, referencingApps) ->
                ServiceInstanceRow(
                    guid = serviceInstance.metadata.guid,
                    name = entity.name,
                    label = serviceLabel,
                    plan = servicePlanName,
                    serviceKeysCount = serviceKeysCount,
                    referencingApps = referencingApps,
                    dashboardUrl = entity.dashboardUrl)
            }
    }

    private fun getServiceLabel(serviceGuid: String?): Mono<String> =
        if (serviceGuid == null) {
            "User-Provided".toMono()
        } else {
            if (labelCache.containsKey(serviceGuid)) {
                labelCache.getValue(serviceGuid).toMono()
            } else {
                cfClient.getService(serviceGuid)
                    .map { it.entity.label }
                    .onErrorReturn("")
                    .doOnNext { labelCache.putIfAbsent(serviceGuid, it) }
            }
        }

    private fun getServicePlan(servicePlanGuid: String?): Mono<String> =
        if (servicePlanGuid == null) {
            "None".toMono()
        } else {
            if (planNameCache.containsKey(servicePlanGuid)) {
                planNameCache.getValue(servicePlanGuid).toMono()
            } else {
                cfClient.getServicePlan(servicePlanGuid)
                    .map { it.entity.name }
                    .doOnNext { planNameCache.putIfAbsent(servicePlanGuid, it) }
            }
        }

    private fun getServiceKeysCount(type: String, serviceKeysUrl: String): Mono<Int> =
        if (type == "managed_service_instance") {
            // No need to paginate, just return the totalResults
            cfClient.getFromCf(path = serviceKeysUrl, bodyType = PaginatedServiceKeys::class.java)
                .map { it.totalResults }
        } else {
            // Service keys are not supported for user-provided service instances
            0.toMono()
        }

    private fun getReferencingApps(serviceBindingsUrl: String): Flux<ApplicationDetail> {
        val bodyType = PaginatedServiceBindings::class.java
        val params = LinkedMultiValueMap<String, String>()
        params.add("results-per-page", "100")

        return cfClient.getFromCf(serviceBindingsUrl, params, bodyType)
            .flatMapMany { response ->
                val firstPage = response.resources.toFlux()
                val otherPages = (2..response.totalPages).toFlux()
                    .parallel()
                    .runOn(scheduler)
                    .flatMap { page ->
                        val newParams = LinkedMultiValueMap<String, String>(params)
                        newParams.add("page", page.toString())

                        cfClient.getFromCf(serviceBindingsUrl, newParams, bodyType)
                            .flatMapMany { it.resources.toFlux() }
                    }

                Flux.merge(firstPage, otherPages)
            }
            .map { it.entity.appGuid }
            .parallel()
            .runOn(scheduler)
            .flatMap { appGuid ->
                if (appCache.containsKey(appGuid)) {
                    appCache.getValue(appGuid).toMono()
                } else {
                    cfClient.getApp(appGuid)
                        .doOnNext { appCache.putIfAbsent(appGuid, it) }
                }
            }
            .sequential()
    }
}
