package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response of /v2/spaces/:spaceGuid/summary
 */
data class SpaceSummary(@JsonProperty("apps") val apps: List<AppOverview>)

data class AppOverview(
        @JsonProperty("guid") val guid: String,
        @JsonProperty("state") val state: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("instances") val instances: Int,
        @JsonProperty("running_instances") val runningInstances: Int,
        @JsonProperty("disk_quota") val diskQuota: Int,
        @JsonProperty("memory") val memory: Int)
