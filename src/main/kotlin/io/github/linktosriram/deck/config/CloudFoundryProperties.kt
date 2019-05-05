package io.github.linktosriram.deck.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cf", ignoreUnknownFields = false)
class CloudFoundryProperties {

    lateinit var apiEndpoint: String

    lateinit var oauthEndpoint: String

    lateinit var username: String

    lateinit var password: String
}
