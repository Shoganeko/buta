package dev.shog.buta.api.obj

import dev.shog.buta.api.obj.msg.MessageHandler
import discord4j.core.event.domain.message.MessageCreateEvent

data class CommandContext(
        val container: MessageHandler.MessageContainer,
        val event: MessageCreateEvent,
        val args: MutableList<String>
)