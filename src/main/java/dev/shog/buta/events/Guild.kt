package dev.shog.buta.events

import dev.shog.buta.EN_US
import dev.shog.buta.LOGGER
import dev.shog.buta.commands.api.GuildFactory
import dev.shog.buta.commands.api.UserFactory
import dev.shog.buta.events.obj.Event
import dev.shog.buta.util.getChannelsWithPermission
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty

/**
 * A guild join event.
 */
object GuildJoinEvent : Event {
    override fun invoke(event: discord4j.core.event.domain.Event): Mono<Void> {
        require(event is GuildCreateEvent)

        return GuildFactory.get(event.guild.id.asLong())
                .switchIfEmpty(
                        getChannelsWithPermission(event.guild)
                                .next()
                                .flatMap { ch -> ch.createMessage(EN_US.get().getString("join-message")) }
                                .flatMap { ch -> ch.guild }
                                .doOnNext { g -> LOGGER.info("Sent join message to ${g.name}") }
                                .flatMap { GuildFactory.create(event.guild.id.asLong()) }
                                .then(GuildFactory.get(event.guild.id.asLong()))
                )
                .then()
    }
}

/**
 * A guild leave event
 */
object GuildLeaveEvent : Event {
    override fun invoke(event: discord4j.core.event.domain.Event): Mono<Void> {
        require(event is GuildDeleteEvent)

        return GuildFactory.delete(event.guildId.asLong())
    }
}