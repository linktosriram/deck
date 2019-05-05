package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/services/:serviceGuid
 */
data class CfService(@JsonProperty("entity") val entity: ServiceEntity)

data class ServiceEntity(@JsonProperty("label") val label: String)
