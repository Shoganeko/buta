package dev.shog.buta.commands.commands.admin

import dev.shog.buta.commands.api.factory.GuildFactory
import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.lib.transport.Duo
import dev.shog.lib.util.toEnabledDisabled
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import reactor.core.publisher.Mono

class SwearFilterCommand : Command(CommandConfig(
        name = "purge",
        desc = "Mass delete commands.",
        category = Category.ADMINISTRATOR,
        permable = PermissionFactory.hasPermission(Permission.ADMINISTRATOR)
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return when {
            args.isEmpty() -> {
                GuildFactory.getObject(e.guildId.get().asLong())
                        .map { obj -> obj.swearFilter }
                        .flatMap { sf -> e.sendMessage(container, "default", sf.first?.toEnabledDisabled(), sf.second) }
            }

            args.getOrNull(0).equals("message", true) && args.size > 1 -> {
                args.removeAt(0)
                val message = args.joinToString(" ")

                GuildFactory.getObject(e.guildId.get().asLong())
                        .doOnNext { obj -> obj.swearFilter = Duo(obj.swearFilter.first, message) }
                        .flatMap { obj -> GuildFactory.updateObject(e.guildId.get().asLong(), obj) }
                        .flatMap { e.sendMessage(container, "message", message) }
                        .then()
            }

            args.getOrNull(0).equals("toggle", true) -> {
                var setTo = false

                GuildFactory.getObject(e.guildId.get().asLong())
                        .doOnNext { obj -> setTo = (obj.swearFilter.first ?: false).not() }
                        .doOnNext { obj -> obj.swearFilter = Duo(setTo, obj.swearFilter.second) }
                        .flatMap { obj ->
                            e.sendMessage(container, "toggle", setTo.toEnabledDisabled())
                                    .flatMap { GuildFactory.updateObject(e.guildId.get().asLong(), obj) }
                        }
                        .then()
            }

            else -> e.sendMessage("error.invalid-arguments").then()
        }
    }

}