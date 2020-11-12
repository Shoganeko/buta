package dev.shog.buta.commands.commands.info

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import kotlin.time.ExperimentalTime

@ExperimentalTime
val PING_COMMAND = Command(CommandConfig("ping", Category.INFO)) {
    event.message.channel.createMessage("pong ${event.gateway.ping.inMilliseconds}ms")
}