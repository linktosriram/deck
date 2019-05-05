package io.github.linktosriram.deck.aggregators

import io.github.linktosriram.deck.CfClient
import io.github.linktosriram.deck.domain.cf.ServiceBinding
import io.github.linktosriram.deck.domain.cf.ServiceInstanceEntity
import io.github.linktosriram.deck.domain.view.ServiceBindingRow
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.core.scheduler.Scheduler
import reactor.util.function.component1
import reactor.util.function.component2
import reactor.util.function.component3
import reactor.util.function.component4

@Service
class ServiceBindingsAggregator(
    private val scheduler: Scheduler,
    private val cfClient: CfClient) {

    fun getServiceBindings(appGuid: String): Flux<ServiceBindingRow> {
        return cfClient.getAppBindings(appGuid)
            .parallel()
            .runOn(scheduler)
            .flatMap { getRow(it) }
            .sequential()
            .subscribeOn(scheduler)
    }

    private fun getRow(binding: ServiceBinding): Mono<ServiceBindingRow> {
        val bindingGuid = binding.metadata.guid
        val credentials = binding.entity.credentials
        val serviceInstanceGuid = binding.entity.serviceInstanceGuid

        return cfClient.getServiceInstance(serviceInstanceGuid)
            .flatMap { serviceInstance ->
                getDetails(serviceInstance.entity, credentials, bindingGuid)
            }
    }

    private fun getDetails(entity: ServiceInstanceEntity, credentials: Map<String, Any>, bindingGuid: String): Mono<ServiceBindingRow> {
        val serviceInstanceName = entity.name
        val servicePlanGuid = entity.servicePlanGuid
        val serviceGuid = entity.serviceGuid

        val label = if (serviceGuid != null) {
            cfClient.getService(serviceGuid)
                .map { it.entity.label }
                .subscribeOn(scheduler)
        } else {
            "User-Provided".toMono()
        }

        val planName = if (servicePlanGuid != null) {
            cfClient.getServicePlan(servicePlanGuid)
                .map { it.entity.name }
                .subscribeOn(scheduler)
        } else {
            "None".toMono()
        }

        return Mono.zip(serviceInstanceName.toMono(), label, planName, credentials.toMono())
            .map { (serviceInstanceName, label, planName, credentials) ->
                ServiceBindingRow(
                    guid = bindingGuid,
                    name = serviceInstanceName,
                    plan = planName,
                    label = label,
                    credentials = credentials,
                    dashboardUrl = entity.dashboardUrl)
            }
    }
}
