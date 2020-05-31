package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage

val WORD_REVERSE_COMMAND = Command(CommandConfig("wordreverse")) {
    if (args.isEmpty())
        sendMessage("include-word")
    else sendMessage("success", buildString {
        args.forEach { append("$it ") }
    }.trim().reversed())
}