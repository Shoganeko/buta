package dev.shog.buta.api.obj

import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import dev.shog.buta.api.obj.msg.MessageHandler

data class CommandContext(
        val container: MessageHandler.MessageContainer,
        val event: MessageCreateEvent,
        val args: MutableList<String>
)