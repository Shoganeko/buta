package dev.shog.buta.commands.commands.admin

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.rest.util.Permission
import java.time.Duration

val PURGE_COMMAND = Command(CommandConfig("purge", PermissionFactory.hasPermission(Permission.ADMINISTRATOR))) {
    val amount = if (args.size == 1) {
        val pre = args[0].toLongOrNull() ?: -1

        if (pre > 500L || 1L > pre) 100 else pre
    } else 100

    event.message.channel
            .ofType(TextChannel::class.java)
            .flatMapMany { ch ->
                ch.bulkDelete(ch.getMessagesBefore(event.message.id)
                        .take(amount)
                        .map { msg -> msg.id }
                )
            }
            .collectList()
            .flatMap { sendMessage("default", amount) }
            .delayElement(Duration.ofSeconds(5))
            .flatMap { msg -> event.message.delete().then(msg.delete()) }
}