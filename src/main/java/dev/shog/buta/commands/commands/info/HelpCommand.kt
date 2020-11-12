package dev.shog.buta.commands.commands.info

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import dev.shog.buta.DEV
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.commands.CommandHandler
import dev.shog.buta.util.addFooter
import dev.shog.buta.util.sendMessage

val HELP_COMMAND = Command(CommandConfig("help", Category.INFO)) {
    if (args.size >= 1) {
        val command = args[0]

        val cmd = CommandHandler.COMMANDS
                .firstOrNull { cmd ->
                    cmd.cfg.name.equals(command, true)
                            && cmd.cfg.permable.hasPermission(event.member ?: return@Command)
                }

        if (cmd != null) {
            cmd.help(event)
        } else {
            sendMessage("Invalid command!")
        }

        return@Command
    }

    val properCategories = Category.values()
            .filter { cat ->
                if (cat == Category.DEVELOPER)
                    DEV.contains(event.member?.id?.longValue ?: return@Command)
                else true
            }

    val helpCommand = properCategories
            .map { cat ->
                cat to CommandHandler.COMMANDS
                        .filter { cmd -> cmd.cfg.category == cat }
                        .filter { cmd -> cmd.cfg.permable.hasPermission(event.member!!) }
                        .joinToString("") { cmd -> "`${cmd.cfg.name}`, " }
                        .removeSuffix(", ")
            }

    event.message.channel.createEmbed {
        addFooter(event)

        title = "Help"
        description = "Support Discord: https://shog.dev/discord\nThis only shows commands **you** have permission to!\n\n"

        helpCommand
                .filter { it.second.isNotEmpty() }
                .forEach {
                    description += "**${it.first}**\n${it.second}\n\n"
                }

        description = description?.removeSuffix("\n\n") ?: ""
    }
}