package dev.shog.buta.commands.commands.admin

import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import reactor.core.publisher.Mono

val SET_PREFIX_COMMAND = Command(CommandConfig("prefix", PermissionFactory.hasPermission(Permission.ADMINISTRATOR))) {
    if (args.size == 1) {
        val newPrefix = args[0]

        if (newPrefix.length > 3 || newPrefix.isEmpty())
            return@Command sendMessage("wrong-length", newPrefix.length)

        event.message.guild
                .map { g -> g.id.asLong() }
                .doOnNext { id -> GuildFactory.getOrCreate(id).prefix = newPrefix }
                .then(sendMessage("set", newPrefix))
    } else event.message.guild
            .map { g -> g.id.asLong() }
            .flatMap { id -> sendMessage("prefix", GuildFactory.getOrCreate(id).prefix) }
}