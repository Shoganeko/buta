package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage

val WORD_REVERSE_COMMAND = Command(CommandConfig("wordreverse", Category.FUN)) {
    if (args.isEmpty())
        sendMessage("Invalid arguments!")
    else buildString {
        args.forEach { append("$it ") }
    }.trim().reversed()
}