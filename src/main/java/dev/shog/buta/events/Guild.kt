package dev.shog.buta.events


import dev.shog.buta.commands.api.factory.GuildFactory
import dev.shog.buta.commands.obj.msg.MessageHandler
import dev.shog.buta.events.obj.Event

import dev.shog.buta.util.getChannelsWithPermission
import dev.shog.buta.util.info
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import reactor.core.publisher.Mono

/**
 * A guild join event.
 */
object GuildJoinEvent : Event {
    override fun invoke(event: discord4j.core.event.domain.Event): Mono<*> {
        require(event is GuildCreateEvent)

        return GuildFactory.getObject(event.guild.id.asLong())
                .hasElement()
                .filter { !it }
                .flatMap {
                    getChannelsWithPermission(event.guild)
                            .next()
                            .flatMap { ch -> ch.createMessage(MessageHandler.getMessage("join-message")) }
                            .flatMap { ch -> ch.guild }
                            .info { g -> "Join message has been sent to ${g.name}." }
                            .flatMap { GuildFactory.createObject(event.guild.id.asLong()) }
                }
                .then()
    }
}

/**
 * A guild leave event
 */
object GuildLeaveEvent : Event {
    override fun invoke(event: discord4j.core.event.domain.Event): Mono<*> {
        require(event is GuildDeleteEvent)

        return GuildFactory.deleteObject(event.guildId.asLong())
    }
}