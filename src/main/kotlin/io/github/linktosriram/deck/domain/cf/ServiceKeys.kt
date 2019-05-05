package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/service_instances/:serviceInstanceGuid/service_keys
 */
data class PaginatedServiceKeys(@JsonProperty("total_results") val totalResults: Int)
