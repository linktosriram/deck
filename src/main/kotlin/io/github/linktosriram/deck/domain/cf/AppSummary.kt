package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/apps/:appGuid/summary
 */
data class ApplicationSummary(
        @JsonProperty("guid") val guid: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("routes") val routes: List<Route>,
        @JsonProperty("running_instances") val runningInstances: Int,
        @JsonProperty("stack_guid") val stackGuid: String,
        @JsonProperty("buildpack") val buildpack: String?,
        @JsonProperty("detected_buildpack") val detectedBuildpack: String?,
        @JsonProperty("memory") val memory: Int,
        @JsonProperty("instances") val instances: Int,
        @JsonProperty("disk_quota") val diskQuota: Int,
        @JsonProperty("state") val state: String,
        @JsonProperty("version") val version: String,
        @JsonProperty("command") val command: String?,
        @JsonProperty("package_state") val packageState: String,
        @JsonProperty("health_check_type") val healthCheckType: String,
        @JsonProperty("health_check_timeout") val healthCheckTimeout: Int?,
        @JsonProperty("health_check_http_endpoint") val healthCheckHttpEndpoint: String?,
        @JsonProperty("docker_image") val dockerImage: String?,
        @JsonProperty("package_updated_at") val packageUpdatedAt: String,
        @JsonProperty("detected_start_command") val detectedStartCommand: String,
        @JsonProperty("enable_ssh") val sshEnabled: Boolean)

data class Route(
        @JsonProperty("host") val host: String,
        @JsonProperty("path") val path: String,
        @JsonProperty("domain") val domain: Domain)

data class Domain(@JsonProperty("name") val name: String)
