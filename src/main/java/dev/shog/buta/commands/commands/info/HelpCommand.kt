package dev.shog.buta.commands.commands.info

import dev.shog.buta.DEV
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.commands.CommandHandler
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.sendMessage
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

val HELP_COMMAND = Command(CommandConfig("help")) {
    if (args.size >= 1) {
        val command = args[0]

        return@Command Flux.fromIterable(CommandHandler.COMMANDS)
                .filter { en -> command.startsWith(en.cfg.name.toLowerCase(), true) }
                .filterWhen { en -> en.cfg.permable.check(event) }
                .collectList()
                .flatMap { list ->
                    if (list.isNotEmpty()) {
                        list[0].help(event)
                    } else event.sendMessage(container, "inv-cmd")
                }
    }

    return@Command Category.values().toFlux()
            .filter { cat ->
                if (cat == Category.DEVELOPER)
                    DEV.contains(event.message.author.get().id.asLong())
                else true
            }
            .flatMap { cat ->
                CommandHandler.COMMANDS.toFlux()
                        .filter { cmd -> Category.valueOf(cmd.container.category.toUpperCase()) == cat }
                        .filterWhen { cmd -> cmd.cfg.permable.check(event) }
                        .map { cmd -> container.getMessage("command", cmd.cfg.name) }
                        .collectList()
                        .filter { list -> list.isNotEmpty() }
                        .map { list ->
                            container.getMessage("cat-pair",
                                    cat.name.capitalize(),
                                    list.asSequence()
                                            .map { str -> str.toLowerCase() }
                                            .joinToString("")
                            )
                        }
                        .map { msg -> msg.substring(0, msg.length - 2) }
            }
            .collectList()
            .map { list -> list.joinToString("\n\n") }
            .flatMap { help ->
                event.message.channel.flatMap { ch ->
                    ch.createEmbed { spec ->
                        container.getEmbed("help-embed").applyEmbed(spec, event.message.author.get(),
                                hashMapOf("desc" to arrayListOf(help)),
                                hashMapOf()
                        )
                    }
                }
            }
}