package dev.shog.buta.events

import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import dev.shog.buta.DEV
import dev.shog.buta.commands.CommandHandler
import dev.shog.buta.api.UserThreadHandler
import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.api.factory.UserFactory
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.msg.MessageHandler
import dev.shog.buta.events.obj.Event
import dev.shog.buta.handle.PlaceholderFiller
import dev.shog.buta.handle.SwearFilter
import dev.shog.buta.util.nullIfBlank
import kotlinx.coroutines.delay

/**
 * A message event.
 */
object MessageEvent : Event {
    override suspend fun invoke(event: com.gitlab.kordlib.core.event.Event) {
        require(event is MessageCreateEvent)

        println(event.message.content)

        val authorId = event.message.author?.id
                ?: return

        val guildId = event.guildId
                ?: return

        val author = UserFactory.getOrCreate(authorId.longValue)
        val guild = GuildFactory.getOrCreate(guildId.longValue)

        val content = event.message.content

        if (SwearFilter.hasSwears(guild, content)) {
            val swearText = PlaceholderFiller.fillText(guild.swearFilterMsg.nullIfBlank()
                    ?: MessageHandler.getMessage("default.swear-filter"), event)
                    ?: "No swearing!! :("

            val message = event.message.channel.createMessage(swearText)
            val permissions = event.getGuild()?.getChannel(event.message.channelId)
                    ?.getEffectivePermissions(event.kord.selfId)
                    ?: return

            if (permissions.contains(Permission.ManageMessages)) {
                delay(5000L)
                message.delete()
            }
        }

        if (content.startsWith(guild.prefix)) {
            val args = content.removePrefix(guild.prefix).split(" ").toMutableList()
            val command = args[0]

            args.removeAt(0)

            val commandObj = CommandHandler.COMMANDS
                    .filter { en ->
                        command.equals(en.cfg.name, true) || en.container.aliases.contains(command)
                    }
                    .singleOrNull { en ->
                        en.cfg.permable.hasPermission(event.member)
                    }

            if (commandObj != null && UserThreadHandler.can(event.member?.asUser()!!, commandObj.cfg.name)) {
                if (commandObj.container.category == Category.DEVELOPER.toString() && !DEV.contains(authorId.longValue)) {
                    event.message.channel.createMessage("This command requires special permissions!")
                    return
                }

                commandObj.invoke(event, args)
                UserThreadHandler.finish(event.member?.asUser()!!, commandObj.container.name)
            }
        }
    }
}