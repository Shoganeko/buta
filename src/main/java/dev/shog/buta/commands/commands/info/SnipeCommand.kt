package dev.shog.buta.commands.commands.info

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.Message
import dev.shog.buta.DELETED_MESSAGES
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.fullUsername

val SNIPE_COMMAND = Command(
    CommandConfig(
        name = "snipe",
        description = "See the most recent deleted message.",
        help = hashMapOf("snipe" to "See the most recent deleted message."),
        category = Category.INFO,
        aliases = listOf("s")
    )
) {
    if (event.guildId != null) {
        val message = DELETED_MESSAGES[event.guildId]

        event.message.channel.createEmbed {
            description = message?.content ?: ""

            author {
                name = message?.author?.fullUsername()
                icon = message?.author?.avatar?.url
            }
        }
    }
}