package dev.shog.buta.events

import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.events.obj.Event
import dev.shog.buta.util.sendMessage

/**
 * When the bot is mentioned, say the current guild's prefix.
 */
object BotMention : Event {
    override suspend fun invoke(event: com.gitlab.kordlib.core.event.Event) {
        require(event is MessageCreateEvent)

        val botId = event.kord.selfId

        if (event.message.mentionedUserIds.contains(botId)) {
            val guild = GuildFactory.getOrCreate(event.guildId?.longValue ?: return)

            val guildName = event.message.getGuild().name

            event.message.channel.createMessage("The prefix for `${guildName}` is `${guild.prefix}`")
        }
    }
}