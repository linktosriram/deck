package io.github.linktosriram.deck.mappers

import io.github.linktosriram.deck.domain.cf.CfEvent
import io.github.linktosriram.deck.domain.view.EventView
import org.springframework.stereotype.Component

@Component
class EventMetadataMapper {

    fun map(event: CfEvent): EventView {
        val entity = event.entity
        val metadata = entity.metadata

        val metadataList = if (metadata.containsKey("request")) {
            @Suppress("UNCHECKED_CAST")
            getList(metadata.getValue("request") as Map<String, Any>)
        } else {
            getList(metadata)
        }

        return EventView(
            type = entity.type,
            actorName = entity.actorName,
            timestamp = entity.timestamp,
            metadata = metadataList)
    }

    private fun getList(m: Map<String, Any>): List<String> =
        m.map { "${it.key}: ${it.value}" }
}
