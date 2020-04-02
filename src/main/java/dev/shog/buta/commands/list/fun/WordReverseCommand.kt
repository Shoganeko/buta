package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class WordReverseCommand : Command(CommandConfig(
        "wordreverse",
        "Reverse a word.",
        Category.FUN,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return if (args.isEmpty())
            e.sendMessage(container, "include-word")
        else e.sendMessage(container, "success", args[0].reversed())
    }
}