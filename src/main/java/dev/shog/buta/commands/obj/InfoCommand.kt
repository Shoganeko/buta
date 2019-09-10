package dev.shog.buta.commands.obj

import discord4j.core.event.domain.message.MessageCreateEvent

/**
 * A info command.
 */
class InfoCommand(
        val commandName: String,
        val commandDesc: String,
        val helpCommand: HashMap<String, String>,
        val isPmAvailable: Boolean,
        val alias: ArrayList<String>,
        val invoke: (Pair<MessageCreateEvent, MutableList<String>>) -> (Unit)
)