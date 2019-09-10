package dev.shog.buta.events

import dev.shog.buta.events.obj.Event
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent

/**
 * A guild join event.
 */
object GuildJoinEvent : Event() {
    override fun invoke(event: discord4j.core.event.domain.Event) {
        require(event is GuildCreateEvent)

        // TODO do event
    }
}

/**
 * A guild leave event
 */
object GuildLeaveEvent : Event() {
    override fun invoke(event: discord4j.core.event.domain.Event) {
        require(event is GuildDeleteEvent)

        // TODO do event
    }
}