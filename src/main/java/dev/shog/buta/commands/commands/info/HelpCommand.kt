package dev.shog.buta.commands.commands.info

import dev.shog.buta.commands.CommandHandler
import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.util.stream.Collectors

class HelpCommand : Command(CommandConfig(
        "help",
        "Help command.",
        Category.INFO,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        if (args.size >= 1) {
            val command = args[0]

            return Flux.fromIterable(CommandHandler.COMMANDS)
                    .filter { en -> command.startsWith(en.cfg.name.toLowerCase(), true) }
                    .filterWhen { en -> en.cfg.permable.check(e) }
                    .collectList()
                    .flatMap { list ->
                        if (list.isNotEmpty()) {
                            list[0].help(e)
                        } else e.sendMessage(container, "inv-cmd")
                    }
        }

        return Category.values().toFlux()
                .flatMap { cat ->
                    CommandHandler.COMMANDS.toFlux()
                            .filter { cmd -> cmd.cfg.category == cat }
                            .filterWhen { cmd -> cmd.cfg.permable.hasPermission(e.message.author.get()) }
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
                    e.message.channel.flatMap { ch ->
                        ch.createEmbed { spec ->
                            container.getEmbed("help-embed").applyEmbed(spec, e.message.author.get(),
                                    hashMapOf("desc" to arrayListOf(help)),
                                    hashMapOf()
                            )
                        }
                    }
                }
    }
}