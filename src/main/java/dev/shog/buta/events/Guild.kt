package dev.shog.buta.events


import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.guild.GuildDeleteEvent
import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.events.obj.Event
import dev.shog.buta.util.getChannelsWithPermission
import kotlinx.coroutines.flow.first

/**
 * A guild join event.
 */
object GuildJoinEvent : Event {
    override suspend fun invoke(event: com.gitlab.kordlib.core.event.Event) {
        require(event is GuildCreateEvent)

        val guild = GuildFactory.getObject(event.guild.id.longValue)

        if (guild == null) {
            GuildFactory.createObject(event.guild.id.longValue)

            getChannelsWithPermission(event.guild)
                    .first()
                    .createMessage("Thank you for inviting me!\nMy name's Buta, and you can view my commands using `b!help`!")

            println("Send join message to ${event.guild.name}") // TODO: Logger
        }
    }
}

/**
 * A guild leave event
 */
object GuildLeaveEvent : Event {
    override suspend fun invoke(event: com.gitlab.kordlib.core.event.Event) {
        require(event is GuildDeleteEvent)

        GuildFactory.deleteObject(event.guildId.longValue)
    }
}