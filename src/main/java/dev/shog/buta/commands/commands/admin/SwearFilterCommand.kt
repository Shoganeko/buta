package dev.shog.buta.commands.commands.admin

import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.toEnabledDisabled
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import reactor.core.publisher.Mono

val SWEAR_FILTER_COMMAND = Command(CommandConfig("swearfilter", PermissionFactory.hasPermission(Permission.ADMINISTRATOR))) {
    return@Command when {
        args.isEmpty() -> {
            val guild = GuildFactory.getOrCreate(event.guildId.get().asLong())

            sendMessage("default", guild.swearFilterOn.toEnabledDisabled(), guild.swearFilterMsg)
        }

        args.getOrNull(0).equals("message", true) && args.size > 1 -> {
            args.removeAt(0)
            val message = args.joinToString(" ")

            GuildFactory.getOrCreate(event.guildId.get().asLong()).swearFilterMsg = message

            sendMessage("message", message)
        }

        args.getOrNull(0).equals("toggle", true) -> {

            val guild = GuildFactory.getOrCreate(event.guildId.get().asLong())

            val setTo = !guild.swearFilterOn
            guild.swearFilterOn = setTo

            sendMessage("toggle", setTo.toEnabledDisabled())
        }

        else -> sendMessage("error.invalid-arguments")
    }
}