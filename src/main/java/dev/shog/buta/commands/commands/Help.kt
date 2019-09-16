package dev.shog.buta.commands.commands

import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.Pre
import dev.shog.buta.util.preset
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.StringBuilder

/**
 * The help command.
 * This views all commands or gets specific command's help.
 */
val HELP = object : Command("Help", "Get help on a command, or view all commands.",
        hashMapOf(Pair("help", "View all commands."), Pair("help [command]", "Get help on a specific command.")),
        true,
        Categories.INFO,
        PermissionFactory.hasPermission(),
        arrayListOf()
) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<Void> {
        if (args.size >= 1) {
            return Flux.fromIterable(COMMANDS)
                    .filter { cmd ->
                        cmd.commandName.equals(args[0], ignoreCase = true)
                    }
                    .collectList()
                    .cache()
                    .flatMap { l ->
                        if (l.isEmpty()) {
                            e.message.channel
                                    .flatMap { ch ->
                                        ch.createMessage(preset(Pre.INV_ARGS, "help [command]", incPrefix = true))
                                    }
                                    .then()
                        } else {
                            l.firstOrNull()?.invokeHelp(e)?.then()
                        }
                    }
                    .then()
        } else {
            val str = StringBuilder().apply {
                append("Invite Buta: https://shog.dev/buta/invite")
                append("\nButa Support: https://shog.dev/buta/discord")
            }

            return Flux.fromIterable(Categories.values().toList())
                    .flatMap { cat ->
                        val category = StringBuilder()

                        Flux.fromIterable(COMMANDS)
                                .filter { cmd -> cmd.category == cat }
                                .filterWhen { cmd -> cmd.permable.check(e) }
                                .collectList()
                                .flatMap { l ->
                                    if (l.isNotEmpty())
                                        Flux.fromIterable(l)
                                                .doFirst { category.append("\n\n**${cat.name.toLowerCase().capitalize()}**\n") }
                                                .doOnNext { cmd -> category.append("`${cmd.commandName}`, ") }
                                                .doFinally { str.append(category.toString().removeSuffix(", ")) }
                                                .then()
                                    else Mono.empty<Void>().then()
                                }
                                .then()
                    }
                    .then(e.message.channel)
                    .flatMap { ch -> ch.createMessage(str.toString()) }
                    .then()
        }
    }
}