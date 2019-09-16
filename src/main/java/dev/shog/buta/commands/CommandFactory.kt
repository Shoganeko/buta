package dev.shog.buta.commands

import dev.shog.buta.commands.obj.*
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/**
 * Gets a command, and turns it into a [Command].
 */
object CommandFactory {
    /**
     * Builds the command.
     *
     * @throws Exception An invalid command as [cmd].
     */
    fun build(cmd: Any): Command {
        when (cmd) {
            // Turns a InfoCommand into a Command.
            is InfoCommand -> {
                return object : Command(cmd.commandName, cmd.commandDesc, cmd.helpCommand, cmd.isPmAvailable, Categories.INFO, cmd.permable, cmd.alias) {
                    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<Void> =
                            cmd.invoke.invoke(Pair(e, args))
                }
            }
        }

        throw Exception("Invalid Command Inputted!")
    }
}