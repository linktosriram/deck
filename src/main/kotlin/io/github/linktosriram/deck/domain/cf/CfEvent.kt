package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/events
 */
data class PaginatedEvents(
        @JsonProperty("total_results") val totalResults: Int,
        @JsonProperty("resources") val resources: List<CfEvent>)

data class CfEvent(@JsonProperty("entity") val entity: CfEventEntity)

data class CfEventEntity(
        @JsonProperty("type") val type: String,
        @JsonProperty("actor_name") val actorName: String,
        @JsonProperty("timestamp") val timestamp: String,
        @JsonProperty("metadata") val metadata: Map<String, Any>)
