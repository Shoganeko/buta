package dev.shog.buta.commands.commands.info

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage

val ABOUT_COMMAND = Command(CommandConfig(
        name = "about",
        category = Category.INFO,
        description = "Get information about Buta.",
        help = hashMapOf("about" to "Get information about Buta.")
)) {
    event.message.channel.createMessage("Buta is a Discord bot made by SHO#0001 using KordLib.")
}