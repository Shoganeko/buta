package dev.shog.buta.commands.commands.info

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage

val ABOUT_COMMAND = Command(CommandConfig("about")) {
    sendMessage("default")
}