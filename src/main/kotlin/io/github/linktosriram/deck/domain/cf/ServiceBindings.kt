package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/service_instances/:serviceInstanceGuid/service_bindings and /v2/apps/:appGuid/service_bindings
 */
data class PaginatedServiceBindings(
        @JsonProperty("total_pages") val totalPages: Int,
        @JsonProperty("resources") val resources: List<ServiceBinding>)

data class ServiceBinding(
        @JsonProperty("metadata") val metadata: Metadata,
        @JsonProperty("entity") val entity: ServiceBindingEntity)

data class ServiceBindingEntity(
        @JsonProperty("app_guid") val appGuid: String,
        @JsonProperty("service_instance_guid") val serviceInstanceGuid: String,
        @JsonProperty("credentials") val credentials: Map<String, Any>)
