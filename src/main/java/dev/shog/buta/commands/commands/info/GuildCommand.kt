package dev.shog.buta.commands.commands.info

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.addFooter
import dev.shog.lib.util.defaultFormat

val GUILD_COMMAND = Command(CommandConfig("guild", Category.INFO)) {
    val guild = event.getGuild() ?: return@Command

    event.message.channel.createEmbed {
        addFooter(event)

        title = "About ${guild.name}"

        field {
            name = "Creation Date"
            value = guild.id.timeStamp.defaultFormat()
        }

        field {
            name = "User Count"
            value = "${guild.memberCount}"
        }
    }
}