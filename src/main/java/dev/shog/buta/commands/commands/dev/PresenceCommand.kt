package dev.shog.buta.commands.commands.dev

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.events.PresenceHandler
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.stream.Collectors

/**
 * Manage presence on Discord.
 */
class PresenceCommand : Command(CommandConfig(
        "presence",
        "Manage presence.",
        Category.DEVELOPER,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return when {
            args.size == 1 && args[0].equals("local", true) -> {
                e.sendMessage(container, "client-side")
                        .doOnNext { PresenceHandler.updatePresences() }
                        .then()
            }

            args.size == 1 && args[0].equals("dump", true) -> {
                PresenceHandler.presences
                        .stream()
                        .map { pres -> "`$pres`" }
                        .collect(Collectors.joining(", "))
                        .toMono()
                        .flatMap { built -> e.sendMessage(container, "dump", built) }
                        .then()
            }

            else -> {
                e.sendMessage(container, "update")
                        .flatMap { PresenceHandler.update(e.client) }
                        .then()
            }
        }
    }
}