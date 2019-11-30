package dev.shog.buta.commands.obj

import dev.shog.buta.commands.CommandFactory
import dev.shog.buta.commands.permission.Permable
import discord4j.core.event.domain.message.MessageCreateEvent
import org.json.JSONObject
import reactor.core.publisher.Mono

/**
 * A info command.
 */
data class InfoCommand(
        val name: String,
        val isPmAvailable: Boolean,
        val permable: Permable,
        val invoke: (Pair<MessageCreateEvent, MutableList<String>>, JSONObject) -> (Mono<Void>)
) {
    /**
     * Build an [InfoCommand]
     */
    fun build() = CommandFactory.build(this)
}