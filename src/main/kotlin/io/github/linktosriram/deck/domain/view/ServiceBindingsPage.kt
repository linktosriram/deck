package io.github.linktosriram.deck.domain.view

import com.fasterxml.jackson.annotation.JsonProperty

data class ServiceBindingsPage(
        @JsonProperty("app_name") val appName: String,
        @JsonProperty("bindings") val bindings: List<ServiceBindingRow>)

data class ServiceBindingRow(
        @JsonProperty("guid") val guid: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("plan") val plan: String,
        @JsonProperty("label") val label: String,
        @JsonProperty("credentials") val credentials: Map<String, Any>,
        @JsonProperty("dashboard_url") val dashboardUrl: String?)
