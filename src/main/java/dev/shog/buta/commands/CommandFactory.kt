package dev.shog.buta.commands

import dev.shog.buta.commands.obj.*
import discord4j.core.event.domain.message.MessageCreateEvent

/**
 * Gets a command, and turns it into a [BuiltCommand].
 */
object CommandFactory {
    /**
     * Builds the command.
     *
     * @throws Exception An invalid command as [cmd].
     */
    fun build(cmd: Any): BuiltCommand {
        when (cmd) {
            is Command -> {
                return object : BuiltCommand() {
                    override suspend fun invoke(e: MessageCreateEvent, args: MutableList<String>) {
                        cmd.invoke(e, args)
                    }

                    override val meta: CommandMeta = CommandMeta(cmd.commandName, cmd.commandDesc, cmd.helpCommand, cmd.isPmAvailable, cmd.category, cmd.alias)
                }
            }

            // Turns a InfoCommand into a Command, then uses this function to turn a Command into a BuiltCommand.
            is InfoCommand -> {
                return build(object : Command(cmd.commandName, cmd.commandDesc, cmd.helpCommand, cmd.isPmAvailable, Categories.INFO, cmd.alias) {
                    override suspend fun invoke(e: MessageCreateEvent, args: MutableList<String>) {
                        cmd.invoke.invoke(Pair(e, args))
                    }
                })
            }
        }

        throw Exception("Invaid Command Inputted!")
    }
}