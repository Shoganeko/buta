package dev.shog.buta.events

import dev.shog.buta.LOGGER
import dev.shog.buta.commands.api.API
import dev.shog.buta.commands.api.guild.GuildFactory
import dev.shog.buta.events.obj.Event
import dev.shog.buta.util.getChannelsWithPermission
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent

/**
 * A guild join event.
 */
object GuildJoinEvent : Event() {
    private const val BUTA_JOIN_MESSAGE = "Hello, my name is Buta!" +
            "\n\nYou can figure out my commands by typing `b!help`."

    override fun invoke(event: discord4j.core.event.domain.Event) {
        require(event is GuildCreateEvent)

        val newToGuild = API.getObject(API.Type.GUILD, event.guild.id.asLong()) != null
        val guild = GuildFactory.getGuild(event.guild.id.asLong())

        if (newToGuild) {
            getChannelsWithPermission(event.guild)
                    .next()
                    .flatMap { ch ->
                        ch.createMessage(BUTA_JOIN_MESSAGE)
                    }.subscribe()
        }
    }
}

/**
 * A guild leave event
 */
object GuildLeaveEvent : Event() {
    override fun invoke(event: discord4j.core.event.domain.Event) {
        require(event is GuildDeleteEvent)

        API.deleteObject(API.Type.GUILD, event.guildId.asLong())
    }
}