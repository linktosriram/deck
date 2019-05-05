package io.github.linktosriram.deck

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.linktosriram.deck.config.CloudFoundryProperties
import io.github.linktosriram.deck.controllers.AppController
import io.github.linktosriram.deck.domain.cf.AppOverview
import io.github.linktosriram.deck.domain.cf.ApplicationDetail
import io.github.linktosriram.deck.domain.cf.ApplicationSummary
import io.github.linktosriram.deck.domain.cf.CfEnvironment
import io.github.linktosriram.deck.domain.cf.CfEvent
import io.github.linktosriram.deck.domain.cf.CfOrganization
import io.github.linktosriram.deck.domain.cf.CfService
import io.github.linktosriram.deck.domain.cf.CfSpace
import io.github.linktosriram.deck.domain.cf.InstanceDetail
import io.github.linktosriram.deck.domain.cf.PaginatedEvents
import io.github.linktosriram.deck.domain.cf.PaginatedOrganizations
import io.github.linktosriram.deck.domain.cf.PaginatedServiceBindings
import io.github.linktosriram.deck.domain.cf.PaginatedServiceInstances
import io.github.linktosriram.deck.domain.cf.PaginatedSpaces
import io.github.linktosriram.deck.domain.cf.ServiceBinding
import io.github.linktosriram.deck.domain.cf.ServiceInstance
import io.github.linktosriram.deck.domain.cf.ServicePlan
import io.github.linktosriram.deck.domain.cf.SpaceSummary
import io.github.linktosriram.deck.domain.cf.TokenResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.scheduler.Scheduler

/**
 * CloudFoundry client which uses the REST APIs.
 */
@Service
class CfClient(
    private val scheduler: Scheduler,
    private val objectMapper: ObjectMapper,
    cfProps: CloudFoundryProperties) {

    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(AppController::class.java)
    }

    private val webClient: WebClient = WebClient.create(cfProps.apiEndpoint)

    private val loginClient: WebClient = WebClient.builder()
        .baseUrl(cfProps.oauthEndpoint)
        .filter(basicAuthentication("cf", ""))
        .build()

    private val tokenRequest: MultiValueMap<String, String> = LinkedMultiValueMap(
        mapOf(
            "username" to listOf(cfProps.username),
            "password" to listOf(cfProps.password),
            "grant_type" to listOf("password")))

    /**
     * Get the Access token at startup.
     * By providing a MultiValueMap<String, String> as the body,
     * Content-Type is automatically set to application/x-www-form-urlencoded by the FormHttpMessageWriter
     */
    private var tokenResponse: TokenResponse = loginClient
        .post()
        .accept(MediaType.APPLICATION_JSON_UTF8)
        .syncBody(tokenRequest)
        .retrieve()
        .bodyToMono(TokenResponse::class.java)
        .block()!!

    private fun <T> retrieve(path: String, params: MultiValueMap<String, String> = LinkedMultiValueMap(), bodyType: Class<T>): Mono<T> {
        return webClient.get()
            .uri { it.path(path).queryParams(params).build() }
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenResponse.accessToken}")
            .retrieve()
            .bodyToMono(bodyType)
            .doOnSubscribe {
                log.info("Getting $path with params $params")
            }
    }

    private fun refreshToken(): Mono<TokenResponse> {
        val refreshRequest: MultiValueMap<String, String> = LinkedMultiValueMap()
        refreshRequest.add("grant_type", "refresh_token")
        refreshRequest.add("refresh_token", tokenResponse.refreshToken)

        return loginClient
            .post()
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .syncBody(refreshRequest)
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
    }

    /**
     * Retrieves the [path] with optional [params] and extract the body to the type [bodyType].
     * Performs an OAuth token refresh using the refresh_token if required.
     * Retries the operation for 3 times in case of error
     */
    fun <T> getFromCf(path: String, params: MultiValueMap<String, String> = LinkedMultiValueMap(), bodyType: Class<T>): Mono<T> {
        return retrieve(path, params, bodyType)
            .onErrorResume(WebClientResponseException.Unauthorized::class.java) {
                log.info("Token Expired. Refreshing...")
                refreshToken()
                    .doOnNext { tokenResponse = it }
                    .flatMap { retrieve(path, params, bodyType) }
            }
            .retry(3) { it !is WebClientResponseException.Forbidden }
    }

    fun listOrganizations(): Flux<CfOrganization> {
        val path = "/v2/organizations"
        val bodyType = PaginatedOrganizations::class.java

        val params = LinkedMultiValueMap<String, String>()
        params.add("results-per-page", "100")

        return getFromCf(path, params, bodyType)
            .flatMapMany { response ->
                val firstPage = response.resources.toFlux()
                val otherPages = (2..response.totalPages).toFlux()
                    .parallel()
                    .runOn(scheduler)
                    .flatMap { page ->
                        val newParams = LinkedMultiValueMap(params)
                        newParams.add("page", page.toString())

                        getFromCf(path, newParams, bodyType)
                            .flatMapMany { it.resources.toFlux() }
                    }

                Flux.merge(firstPage, otherPages)
            }
    }

    fun listSpaces(orgGuid: String): Flux<CfSpace> {
        val path = "/v2/spaces"
        val bodyType = PaginatedSpaces::class.java

        val params = LinkedMultiValueMap<String, String>()
        params.add("q", "organization_guid:$orgGuid")
        params.add("results-per-page", "100")

        return getFromCf(path, params, bodyType)
            .flatMapMany { response ->
                val firstPage = response.resources.toFlux()
                val otherPages = (2..response.totalPages).toFlux()
                    .parallel()
                    .runOn(scheduler)
                    .flatMap { page ->
                        val newParams = LinkedMultiValueMap(params)
                        newParams.add("page", page.toString())

                        getFromCf(path, newParams, bodyType)
                            .flatMapMany { it.resources.toFlux() }
                    }

                Flux.merge(firstPage, otherPages)
            }
    }

    fun getSpace(spaceGuid: String): Mono<CfSpace> {
        return getFromCf(path = "/v2/spaces/$spaceGuid", bodyType = CfSpace::class.java)
    }

    fun listApps(spaceGuid: String): Flux<AppOverview> {
        return getFromCf(path = "/v2/spaces/$spaceGuid/summary", bodyType = SpaceSummary::class.java)
            .flatMapMany { it.apps.toFlux() }
    }

    fun getAppSummary(appGuid: String): Mono<ApplicationSummary> {
        return getFromCf(path = "/v2/apps/$appGuid/summary", bodyType = ApplicationSummary::class.java)
    }

    fun getApp(appGuid: String): Mono<ApplicationDetail> {
        return getFromCf(path = "/v2/apps/$appGuid", bodyType = ApplicationDetail::class.java)
    }

    fun getInstanceDetails(appGuid: String): Flux<InstanceDetail> {
        // The Response from CF is a JSON Object with ordinal keys "0", "1" ... etc
        // So, we extract the body as a [LinkedHashMap] and then extract all the values to [InstanceDetail]
        return getFromCf(path = "/v2/apps/$appGuid/stats", bodyType = LinkedHashMap::class.java)
            .flatMapMany { stat ->
                stat.values
                    .map { v -> objectMapper.convertValue(v, InstanceDetail::class.java) }
                    .toFlux()
            }
    }

    fun recentEvents(appGuid: String): Flux<CfEvent> {
        val params = LinkedMultiValueMap<String, String>()

        params.add("q", "actee:$appGuid")
        params.add("order-by", "timestamp")
        params.add("order-direction", "desc")
        params.add("results-per-page", "5")

        return getFromCf("/v2/events", params, PaginatedEvents::class.java)
            .flatMapMany { it.resources.toFlux() }
    }

    fun getAppBindings(appGuid: String): Flux<ServiceBinding> {
        val path = "/v2/apps/$appGuid/service_bindings"
        val bodyType = PaginatedServiceBindings::class.java

        val params = LinkedMultiValueMap<String, String>()
        params.add("results-per-page", "100")

        return getFromCf(path, params, bodyType)
            .flatMapMany { response ->
                val firstPage = response.resources.toFlux()

                val otherPages = (2..response.totalPages).toFlux()
                    .parallel()
                    .runOn(scheduler)
                    .flatMap { page ->
                        val newParams = LinkedMultiValueMap(params)
                        newParams.add("page", page.toString())

                        getFromCf(path, newParams, bodyType)
                            .flatMapMany { it.resources.toFlux() }
                    }

                Flux.merge(firstPage, otherPages)
            }
    }

    fun getEnv(appGuid: String): Mono<CfEnvironment> {
        return getFromCf(path = "/v2/apps/$appGuid/env", bodyType = CfEnvironment::class.java)
    }

    fun appEvents(appGuid: String, page: Int): Mono<PaginatedEvents> {
        val params = LinkedMultiValueMap<String, String>()
        params.add("q", "actee:$appGuid")
        params.add("order-by", "timestamp")
        params.add("order-direction", "desc")
        params.add("page", page.toString())
        params.add("results-per-page", "100")

        return getFromCf("/v2/events", params, PaginatedEvents::class.java)
    }

    fun listServiceInstances(spaceGuid: String): Flux<ServiceInstance> {
        val path = "/v2/spaces/$spaceGuid/service_instances"
        val bodyType = PaginatedServiceInstances::class.java

        val params = LinkedMultiValueMap<String, String>()
        params.add("return_user_provided_service_instances", "true")
        params.add("results-per-page", "100")

        return getFromCf(path, params, bodyType)
            .flatMapMany { response ->
                val firstPage = response.resources.toFlux()
                val otherPages = (2..response.totalPages).toFlux()
                    .parallel()
                    .runOn(scheduler)
                    .flatMap { page ->
                        val newParams = LinkedMultiValueMap(params)
                        newParams.add("page", page.toString())

                        getFromCf(path, newParams, bodyType)
                            .flatMapMany { it.resources.toFlux() }
                    }

                Flux.merge(firstPage, otherPages)
            }
    }

    fun getServiceInstance(serviceInstanceGuid: String): Mono<ServiceInstance> {
        return getFromCf(path = "/v2/service_instances/$serviceInstanceGuid", bodyType = ServiceInstance::class.java)
    }

    fun getService(serviceGuid: String): Mono<CfService> {
        return getFromCf(path = "/v2/services/$serviceGuid", bodyType = CfService::class.java)
    }

    fun getServicePlan(servicePlanGuid: String): Mono<ServicePlan> {
        return getFromCf(path = "/v2/service_plans/$servicePlanGuid", bodyType = ServicePlan::class.java)
    }
}
