package dev.shog.buta.commands.commands.dev

import dev.shog.buta.commands.api.UserThreadHandler
import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.getOrNull
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class ThreadViewCommand : Command(CommandConfig(
        "viewthreads",
        "View threads",
        Category.DEVELOPER,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        if (args.size == 1 && args[0].equals("clear", true)) {
            return e.sendMessage(container, "cleared")
                    .doOnNext { UserThreadHandler.users.remove(e.message.author.get()) }
        }

        return e.sendMessage(container, "response", UserThreadHandler.users.getOrNull(e.message.author.get()))
    }
}