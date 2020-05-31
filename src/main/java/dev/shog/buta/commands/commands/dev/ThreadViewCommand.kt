package dev.shog.buta.commands.commands.dev

import dev.shog.buta.api.UserThreadHandler
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.getOrNull
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

val THREAD_VIEW_COMMAND = Command(CommandConfig("viewthreads")) {
    if (args.size == 1 && args[0].equals("clear", true)) {
        return@Command sendMessage("cleared")
                .doOnNext { UserThreadHandler.users.remove(event.message.author.get()) }
    }

    sendMessage("response", UserThreadHandler.users.getOrNull(event.message.author.get()))
}