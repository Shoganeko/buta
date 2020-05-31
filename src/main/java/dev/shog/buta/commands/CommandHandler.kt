package dev.shog.buta.commands

import dev.shog.buta.api.obj.Command

/**
 * A command handler.
 */
object CommandHandler {
    val COMMANDS = arrayListOf<Command>()

    /**
     * Add many [cmd] to [COMMANDS].
     */
    fun add(vararg cmd: Command) {
        cmd.forEach { c -> COMMANDS.add(c) }
    }
}