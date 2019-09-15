package dev.shog.buta.commands.obj

import discord4j.core.event.domain.message.MessageCreateEvent

/**
 * The main command interface.
 *
 * The command can be invoked by a user sending a message with the prefix then [commandName] in a channel where Buta can see it.
 *
 * If [isPmAvailable] is true, it can be accessible through pms.
 */
abstract class Command(
        val commandName: String,
        val commandDesc: String,
        val helpCommand: HashMap<String, String>,
        val isPmAvailable: Boolean,
        val category: Categories,
        val alias: ArrayList<String>
) {
    /**
     * When the command is invoked by a user.
     */
    abstract suspend fun invoke(e: MessageCreateEvent, args: MutableList<String>)

    companion object {
        /**
         * All of the commands.
         */
        val COMMANDS = ArrayList<BuiltCommand>()
    }
}