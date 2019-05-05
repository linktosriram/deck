package io.github.linktosriram.deck

import io.github.linktosriram.deck.config.CloudFoundryProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableConfigurationProperties(CloudFoundryProperties::class)
class DeckApplication {

    companion object {
        @JvmStatic
        val log: Logger = LoggerFactory.getLogger(DeckApplication::class.java)
    }

    @Bean
    fun runner() = CommandLineRunner {
        val runtime = Runtime.getRuntime()

        log.info("************************")
        log.info("Available processors: ${runtime.availableProcessors()}")
        log.info("Total memory: ${runtime.totalMemory() / 1048576} MB")
        log.info("Free memory: ${runtime.freeMemory() / 1048576} MB")
        log.info("Max memory: ${runtime.maxMemory() / 1048576} MB")
        log.info("************************")
    }
}

fun main(args: Array<String>) {
    runApplication<DeckApplication>(*args)
}
