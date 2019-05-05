package io.github.linktosriram.deck.domain.view

import com.fasterxml.jackson.annotation.JsonProperty

data class EventsPage(
        @JsonProperty("app_name") val appName: String,
        @JsonProperty("events") val events: PaginatedEventView)

data class PaginatedEventView(
        @JsonProperty("total_results") val totalResults: Int,
        @JsonProperty("resources") val resources: List<EventView>)

data class EventView(
        @JsonProperty("type") val type: String,
        @JsonProperty("actor_name") val actorName: String,
        @JsonProperty("timestamp") val timestamp: String,
        @JsonProperty("metadata") val metadata: List<String>)
