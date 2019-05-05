package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/service_plans/:servicePlanGuid
 */
data class ServicePlan(@JsonProperty("entity") val entity: ServicePlanEntity)

data class ServicePlanEntity(@JsonProperty("name") val name: String)
