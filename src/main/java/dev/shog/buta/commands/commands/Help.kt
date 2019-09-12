package dev.shog.buta.commands.commands

import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.util.Pre
import dev.shog.buta.util.preset
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Flux
import java.lang.StringBuilder

/**
 * The help command.
 * This views all commands or gets specific command's help.
 */
val HELP = object : Command("Help", "Get help on a command, or view all commands.",
        hashMapOf(Pair("help", "View all commands."), Pair("help [command]", "Get help on a specific command.")),
        true, Categories.INFO, arrayListOf()
) {
    override suspend fun invoke(e: MessageCreateEvent, args: MutableList<String>) {
        if (args.size >= 1) {
            Flux.fromIterable(COMMANDS)
                    .filter { cmd ->
                        cmd.meta.commandName.equals(args[0], ignoreCase = true)
                    }
                    .collectList()
                    .cache()
                    .subscribe { l ->
                        if (l.isEmpty()) {
                            e.message.channel
                                    .flatMap { ch ->
                                        ch.createMessage(preset(Pre.INV_ARGS, "help [command]", incPrefix = true))
                                    }
                                    .subscribe()
                        } else {
                            l.firstOrNull()?.invokeHelp(e)
                        }
                    }

            return
        } else {
            val str = StringBuilder().apply {
                append("Invite Buta: https://shog.dev/buta/invite")
                append("\nButa Support: https://shog.dev/buta/discord")
            }

            for (cat in Categories.values()) {
                val category = StringBuilder()

                Flux.fromIterable(COMMANDS)
                        .filter { cmd ->
                            cmd.meta.category == cat
                        }
                        .collectList()
                        .subscribe { l ->
                            if (l.isNotEmpty()) {
                                category.append("\n\n**${cat.name.toLowerCase().capitalize()}**\n")

                                Flux.fromIterable(l)
                                        .filter { cmd ->
                                            // TODO see if user has permission
                                            true
                                        }
                                        .doOnNext { cmd ->
                                            category.append("`${cmd.meta.commandName}`, ")
                                        }
                                        .doFinally {
                                            str.append(category.toString().removeSuffix(", "))
                                        }
                                        .subscribe()
                            }
                        }
            }

            e.message.channel
                    .flatMap { ch ->
                        ch.createMessage(str.toString())
                    }.subscribe()
        }
    }
}