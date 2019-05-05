package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from /v2/apps/:appGuid/stats
 */
data class InstanceDetail(
        @JsonProperty("state") val state: String,
        @JsonProperty("stats") val stats: InstanceStats)

data class InstanceStats(
        @JsonProperty("host") val host: String,
        @JsonProperty("port") val port: Int,
        @JsonProperty("uptime") val uptime: Long,
        @JsonProperty("mem_quota") val memoryQuota: Long,
        @JsonProperty("disk_quota") val diskQuota: Long,
        @JsonProperty("fds_quota") val fdsQuota: Long,
        @JsonProperty("usage") val usage: InstanceUsage)

data class InstanceUsage(
        @JsonProperty("disk") val disk: Long,
        @JsonProperty("mem") val memory: Long,
        @JsonProperty("cpu") val cpu: Double)
