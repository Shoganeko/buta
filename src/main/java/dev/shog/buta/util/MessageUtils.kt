package dev.shog.buta.util

import com.gitlab.kordlib.core.entity.Message
import dev.shog.buta.api.obj.CommandContext


/**
 * Send [link] with [args].
 */
suspend fun CommandContext.sendMessage(message: String): Message =
        event.message.channel.createMessage(message)