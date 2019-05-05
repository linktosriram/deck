package io.github.linktosriram.deck.domain.view

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.linktosriram.deck.domain.cf.AppOverview

data class AppListing(
        @JsonProperty("space_name") val spaceName: String,
        @JsonProperty("apps") val apps: List<AppOverview>)
