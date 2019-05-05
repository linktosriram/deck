package io.github.linktosriram.deck.controllers

import io.github.linktosriram.deck.domain.cf.CfOrganization
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/organizations")
class OrganizationController(private val cfClient: io.github.linktosriram.deck.CfClient) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun listOrganizations(): Flux<CfOrganization> {
        return cfClient.listOrganizations()
    }
}
