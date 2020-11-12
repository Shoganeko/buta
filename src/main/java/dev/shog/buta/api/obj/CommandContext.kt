package dev.shog.buta.api.obj

import com.gitlab.kordlib.core.event.message.MessageCreateEvent

data class CommandContext(
        val event: MessageCreateEvent,
        val args: MutableList<String>
)