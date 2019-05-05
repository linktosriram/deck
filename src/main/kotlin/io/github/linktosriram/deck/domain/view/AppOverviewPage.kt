package io.github.linktosriram.deck.domain.view

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.linktosriram.deck.domain.cf.ApplicationSummary
import io.github.linktosriram.deck.domain.cf.InstanceDetail

data class AppOverviewPage(
    @JsonProperty("summary") val summary: ApplicationSummary,
    @JsonProperty("instances") val instances: List<InstanceDetail>,
    @JsonProperty("events") val events: List<EventView>)
