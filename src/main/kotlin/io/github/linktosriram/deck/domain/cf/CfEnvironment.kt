package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/apps/:appGuid/env
 */
data class CfEnvironment(
        @JsonProperty("staging_env_json") val staging: Map<String, Any>,
        @JsonProperty("running_env_json") val running: Map<String, Any>,
        @JsonProperty("environment_json") val environment: Map<String, Any>,
        @JsonProperty("system_env_json") val system: Map<String, Any>,
        @JsonProperty("application_env_json") val application: Map<String, Any>)
