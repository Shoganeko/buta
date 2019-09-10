package dev.shog.buta.commands.commands

import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.util.Pre
import dev.shog.buta.util.preset
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Flux

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
            synchronized(COMMANDS) {
                for (command in COMMANDS) {
                    if (command.meta.commandName.equals(args[0], ignoreCase = true)) {
                        command.invokeHelp(e)
                        return
                    }
                }
            }

            e.message.channel
                    .flatMap { ch ->
                        ch.createMessage(preset(Pre.INV_ARGS, "help [command]", incPrefix = true))
                    }
                    .subscribe()

            return
        } else {
            // TODO build help message
        }
    }
}