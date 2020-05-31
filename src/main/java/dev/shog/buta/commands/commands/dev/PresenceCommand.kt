package dev.shog.buta.commands.commands.dev

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.events.PresenceHandler
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.stream.Collectors

/**
 * Manage presence on Discord.
 */
val PRESENCE_COMMAND = Command(CommandConfig("presence")) {
    return@Command when {
        args.size == 1 && args[0].equals("local", true) -> {
            sendMessage("client-side")
                    .doOnNext { PresenceHandler.updatePresences() }
                    .then()
        }

        args.size == 1 && args[0].equals("dump", true) -> {
            PresenceHandler.presences
                    .stream()
                    .map { pres -> "`$pres`" }
                    .collect(Collectors.joining(", "))
                    .toMono()
                    .flatMap { built -> sendMessage("dump", built) }
                    .then()
        }

        else -> {
            sendMessage("update")
                    .flatMap { PresenceHandler.update(event.client) }
                    .then()
        }
    }
}