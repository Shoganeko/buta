package dev.shog.buta.commands.commands.gamble

import com.gitlab.kordlib.core.any
import dev.shog.buta.api.factory.UserFactory
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage
import kotlinx.coroutines.flow.first

val BALANCE_COMMAND = Command(
    CommandConfig(
        name = "balance",
        description = "See your balance.",
        category = Category.GAMBLING,
        aliases = listOf("bal"),
        help = hashMapOf(
            "balance" to "See your own balance.",
            "balance {@user}" to "See another user's balance."
        )
    )
) {
    if (event.message.mentionedUserIds.any()) {
        event.message.mentionedUsers
            .first { user ->
                val balance = UserFactory.getOrCreate(user.id.longValue).bal

                sendMessage("`${user.username}#${user.discriminator}`'s balance is `${balance}")

                true
            }
    } else {
        val balance = UserFactory.getOrCreate(event.message.author?.id?.longValue ?: return@Command).bal

        sendMessage("Your balance is `$balance`.")
    }
}