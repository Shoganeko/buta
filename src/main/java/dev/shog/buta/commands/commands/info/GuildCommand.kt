package dev.shog.buta.commands.commands.info

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.rest.Image
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.addFooter
import dev.shog.lib.util.defaultFormat

val GUILD_COMMAND = Command(CommandConfig(
        name = "guild",
        category = Category.INFO,
        description = "Get information about a guild.",
        help = hashMapOf("guild" to "Get information about the current guild.")
)) {
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

        image = guild.getIconUrl(Image.Format.JPEG)
    }
}