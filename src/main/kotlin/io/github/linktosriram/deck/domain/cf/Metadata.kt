package io.github.linktosriram.deck.domain.cf

import com.fasterxml.jackson.annotation.JsonProperty

data class Metadata(@JsonProperty("guid") val guid: String)
