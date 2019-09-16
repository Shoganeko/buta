package dev.shog.buta.commands.obj

import dev.shog.buta.commands.permission.Permable
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/**
 * A info command.
 */
class InfoCommand(
        val commandName: String,
        val commandDesc: String,
        val helpCommand: HashMap<String, String>,
        val isPmAvailable: Boolean,
        val permable: Permable,
        val alias: ArrayList<String>,
        val invoke: (Pair<MessageCreateEvent, MutableList<String>>) -> (Mono<Void>)
)