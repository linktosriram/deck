package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/spaces/:spaceGuid/service_instances
 */
data class PaginatedServiceInstances(
        @JsonProperty("total_pages") val totalPages: Int,
        @JsonProperty("resources") val resources: List<ServiceInstance>)

/**
 * Response from /v2/service_instances/:serviceInstanceGuid
 */
data class ServiceInstance(
        @JsonProperty("metadata") val metadata: Metadata,
        @JsonProperty("entity") val entity: ServiceInstanceEntity)

data class ServiceInstanceEntity(
        @JsonProperty("name") val name: String,
        @JsonProperty("dashboard_url") val dashboardUrl: String?,
        @JsonProperty("type") val type: String,
        @JsonProperty("service_guid") val serviceGuid: String?,
        @JsonProperty("service_plan_guid") val servicePlanGuid: String?,
        @JsonProperty("service_bindings_url") val serviceBindingsUrl: String,
        @JsonProperty("service_keys_url") val serviceKeysUrl: String)
