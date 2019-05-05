package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from authorization_endpoint/oauth/token
 */
data class TokenResponse(@JsonProperty("access_token") val accessToken: String,
                         @JsonProperty("refresh_token") val refreshToken: String)
