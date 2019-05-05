package io.github.linktosriram.deck.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Configuration
class ReactorConfig {

    /**
     * An elastic thread pool creates new worker pools as needed, and reuse idle ones.
     * Worker pools that stay idle for too long (60s) are disposed.
     */
    @Bean
    fun cappedElasticScheduler(): Scheduler {
        val executor = ThreadPoolExecutor(
                (5 * availableProcessors()),
                (15 * availableProcessors()),
                60L,
                TimeUnit.SECONDS,
                SynchronousQueue<Runnable>(),
                daemonThreadFactory("io-pool"),
                ThreadPoolExecutor.CallerRunsPolicy())

        return Schedulers.fromExecutorService(executor)
    }

    private fun availableProcessors(): Int =
            Runtime.getRuntime().availableProcessors()

    private fun daemonThreadFactory(name: String): ThreadFactory {
        val count = AtomicInteger()

        return ThreadFactory { runnable ->
            val thread = Thread(runnable)
            thread.name = "$name-${count.incrementAndGet()}"
            thread.isDaemon = true
            thread
        }
    }
}
