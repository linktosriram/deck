package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/spaces
 */
data class PaginatedSpaces(
        @JsonProperty("total_pages") val totalPages: Int,
        @JsonProperty("resources") val resources: List<CfSpace>)

data class CfSpace(
        @JsonProperty("metadata") val metadata: Metadata,
        @JsonProperty("entity") val entity: CfSpaceEntity)

data class CfSpaceEntity(@JsonProperty("name") val name: String)
