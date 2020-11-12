package dev.shog.buta.commands.commands.admin

import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.core.entity.channel.GuildMessageChannel
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

@ExperimentalCoroutinesApi
val PURGE_COMMAND = Command(CommandConfig(
        name = "purge",
        description = "Delete a specified amount of messages",
        category = Category.ADMINISTRATOR,
        help = hashMapOf(
                "purge" to "Delete 100 messages in the current channel.",
                "purge {number}" to "Delete a specified amount of messages in the current channel."
        ),
        permable = PermissionFactory.hasPermission(Permission.Administrator)
)) {
    val amount = if (args.size == 1) {
        val pre = args[0].toLongOrNull() ?: -1

        if (pre > 500L || 1L > pre) 100 else pre
    } else 100

    val channel = event.message.channel as GuildMessageChannel

    channel.bulkDelete(
            channel.getMessagesBefore(event.message.id)
                    .take(amount.toInt())
                    .map { msg -> msg.id }
                    .toList()
    )

    val botMessage = sendMessage("Removed the last `${amount}` messages.")

    delay(1000L)

    event.message.delete()
    botMessage.delete()
}