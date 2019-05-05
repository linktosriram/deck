package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/apps/:appGuid
 */
data class ApplicationDetail(
        @JsonProperty("metadata") val metadata: Metadata,
        @JsonProperty("entity") val entity: ApplicationDetailEntity)

data class ApplicationDetailEntity(@JsonProperty("name") val name: String)
