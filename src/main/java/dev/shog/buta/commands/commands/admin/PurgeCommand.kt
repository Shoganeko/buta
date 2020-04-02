package dev.shog.buta.commands.commands.admin

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import reactor.core.publisher.Mono
import java.time.Duration

class PurgeCommand : Command(CommandConfig(
        name = "purge",
        desc = "Mass delete commands.",
        category = Category.ADMINISTRATOR,
        permable = PermissionFactory.hasPermission(Permission.ADMINISTRATOR)
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        val amount = if (args.size == 1) {
            val pre = args[0].toLongOrNull() ?: -1

            if (pre > 500L || 1L > pre) 100 else pre
        } else 100

        return e.message.channel
                .ofType(TextChannel::class.java)
                .flatMapMany { ch ->
                    ch.bulkDelete(ch.getMessagesBefore(e.message.id)
                            .take(amount)
                            .map { msg -> msg.id }
                    )
                }
                .collectList()
                .flatMap { e.sendMessage(container, "default", amount) }
                .delayElement(Duration.ofSeconds(5))
                .flatMap { msg -> e.message.delete().then(msg.delete()) }
    }
}