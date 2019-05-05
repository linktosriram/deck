package io.github.linktosriram.deck.domain.view

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.linktosriram.deck.domain.cf.ApplicationDetail

data class ServiceInstanceListing(
        @JsonProperty("space_name") val spaceName: String,
        @JsonProperty("service_instances") val serviceInstances: List<ServiceInstanceRow>)

data class ServiceInstanceRow(
    @JsonProperty("guid") val guid: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("label") val label: String,
    @JsonProperty("plan") val plan: String,
    @JsonProperty("referencing_apps") val referencingApps: List<ApplicationDetail>,
    @JsonProperty("service_keys_count") val serviceKeysCount: Int,
    @JsonProperty("dashboard_url") val dashboardUrl: String?)
