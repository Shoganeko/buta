package dev.shog.buta.commands.commands.admin

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.toEnabledDisabled
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import reactor.core.publisher.Mono

class NsfwToggleCommand : Command(CommandConfig(
        name = "nsfw",
        desc = "Toggle NSFW in a channel",
        category = Category.ADMINISTRATOR,
        permable = PermissionFactory.hasPermission(Permission.ADMINISTRATOR)
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return e.message.channel
                .ofType(TextChannel::class.java)
                .flatMap { ch ->
                    e.sendMessage(container, "default", ch.isNsfw.not().toEnabledDisabled())
                            .then(ch.edit { che -> che.setNsfw(!ch.isNsfw) })
                }
    }
}