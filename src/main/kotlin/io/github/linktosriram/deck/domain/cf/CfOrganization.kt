package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/organizations
 */
data class PaginatedOrganizations(
        @JsonProperty("total_pages") val totalPages: Int,
        @JsonProperty("resources") val resources: List<CfOrganization>)

data class CfOrganization(
        @JsonProperty("metadata") val metadata: Metadata,
        @JsonProperty("entity") val entity: CfOrganizationEntity)

data class CfOrganizationEntity(@JsonProperty("name") val name: String)
