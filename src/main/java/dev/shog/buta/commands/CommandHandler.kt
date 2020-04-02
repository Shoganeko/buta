package dev.shog.buta.commands

import dev.shog.buta.commands.obj.Command

object CommandHandler {
    val COMMANDS = arrayListOf<Command>()

    fun add(vararg cmd: Command) {
        cmd.forEach { c -> COMMANDS.add(c) }
    }
}