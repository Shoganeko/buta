package dev.shog.buta.commands.commands.info

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import kotlinx.coroutines.flow.first

val AVATAR_COMMAND = Command(
    CommandConfig(
        name = "avatar",
        aliases = listOf("av"),
        category = Category.INFO,
        description = "Get the avatar of a user.",
        help = hashMapOf(
            "avatar" to "Get your own avatar.",
            "avatar {@user}" to "Get the avatar of another user."
        )
    )
) {
    if (event.message.mentionedUserIds.isNotEmpty()) {
        event.message.mentionedUsers
            .first { user ->
                event.message.channel.createEmbed {
                    title = "${user.username}#${user.discriminator}'s avatar"

                    image = user.avatar.url
                }

                true
            }

        return@Command
    }

    event.message.channel.createEmbed {
        title = "${event.member?.username}#${event.member?.discriminator}'s avatar"

        image = event.member?.avatar?.url
    }
}