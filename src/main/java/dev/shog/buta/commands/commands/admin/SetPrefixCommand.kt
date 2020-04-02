package dev.shog.buta.commands.commands.admin

import dev.shog.buta.commands.api.factory.GuildFactory
import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import reactor.core.publisher.Mono

class SetPrefixCommand : Command(CommandConfig(
        name = "purge",
        desc = "Mass delete commands.",
        category = Category.ADMINISTRATOR,
        permable = PermissionFactory.hasPermission(Permission.ADMINISTRATOR)
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return if (args.size == 1) {
            val newPrefix = args[0]

            if (newPrefix.length > 3 || newPrefix.isEmpty())
                return e.sendMessage(container, "wrong-length", newPrefix.length)

            e.message.guild
                    .map { g -> g.id.asLong() }
                    .flatMap { id -> GuildFactory.getObject(id) }
                    .doOnNext { g -> g.prefix = newPrefix }
                    .flatMap { g -> GuildFactory.updateObject(g.id, g) }
                    .then(e.sendMessage(container, "set", newPrefix))
        } else e.message.guild
                .map { g -> g.id.asLong() }
                .flatMap(GuildFactory::getObject)
                .map { g -> g.prefix }
                .flatMap { p -> e.sendMessage(container, "prefix", p) }
    }

}