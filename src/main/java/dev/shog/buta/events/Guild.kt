package dev.shog.buta.events

import dev.shog.buta.commands.api.API
import dev.shog.buta.commands.api.guild.GuildFactory
import dev.shog.buta.events.obj.Event
import dev.shog.buta.util.getChannelsWithPermission
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * A guild join event.
 */
object GuildJoinEvent : Event() {
    private const val BUTA_JOIN_MESSAGE = "Hello, my name is Buta!" +
            "\n\nYou can figure out my commands by typing `b!help`."

    override fun invoke(event: discord4j.core.event.domain.Event) {
        require(event is GuildCreateEvent)

        Mono.justOrEmpty(event.guild).subscribe { g ->
            GuildFactory.getGuild(g.id.asLong())
                    .switchIfEmpty(
                            getChannelsWithPermission(g)
                                    .next()
                                    .flatMap { ch -> ch.createMessage(BUTA_JOIN_MESSAGE) }
                                    .then(Mono.empty())
                    ).subscribe()
        }
    }
}

/**
 * A guild leave event
 */
object GuildLeaveEvent : Event() {
    override fun invoke(event: discord4j.core.event.domain.Event) {
        require(event is GuildDeleteEvent)

        API.deleteObject(API.Type.GUILD, event.guildId.asLong()).subscribe()
    }
}