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
        name = "swearfilter",
        desc = "Manage the swear filter.",
        category = Category.ADMINISTRATOR,
        permable = PermissionFactory.hasPermission(Permission.ADMINISTRATOR)
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return when {
            args.isEmpty() -> {
                val guild = GuildFactory.getOrCreate(e.guildId.get().asLong())

                e.sendMessage(container, "default", guild.swearFilterOn.toEnabledDisabled(), guild.swearFilterMsg)
            }

            args.getOrNull(0).equals("message", true) && args.size > 1 -> {
                args.removeAt(0)
                val message = args.joinToString(" ")

                GuildFactory.getOrCreate(e.guildId.get().asLong()).swearFilterMsg = message

                e.sendMessage(container, "message", message)
            }

            args.getOrNull(0).equals("toggle", true) -> {

                val guild = GuildFactory.getOrCreate(e.guildId.get().asLong())

                val setTo = !guild.swearFilterOn
                guild.swearFilterOn = setTo

                e.sendMessage(container, "toggle", setTo.toEnabledDisabled())
            }

            else -> e.sendMessage("error.invalid-arguments").then()
        }
    }

}