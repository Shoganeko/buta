package dev.shog.buta.commands.commands.admin

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.toEnabledDisabled
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import reactor.core.publisher.Mono

val NSFW_TOGGLE_COMMAND = Command(CommandConfig("nsfw", PermissionFactory.hasPermission(Permission.ADMINISTRATOR))) {
    event.message.channel
            .ofType(TextChannel::class.java)
            .flatMap { ch ->
                sendMessage("default", ch.isNsfw.not().toEnabledDisabled())
                        .then(ch.edit { che -> che.setNsfw(!ch.isNsfw) })
            }
}