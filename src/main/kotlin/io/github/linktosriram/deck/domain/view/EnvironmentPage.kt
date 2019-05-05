package io.github.linktosriram.deck.domain.view

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.linktosriram.deck.domain.cf.CfEnvironment

data class EnvironmentPage(
        @JsonProperty("app_name") val appName: String,
        @JsonProperty("env") val env: CfEnvironment)
