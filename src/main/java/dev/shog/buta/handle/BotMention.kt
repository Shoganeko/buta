package dev.shog.buta.handle

import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.events.obj.Event
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/**
 * When the bot is mentioned, say the current guild's prefix.
 */
object BotMention : Event {
    override fun invoke(event: discord4j.core.event.domain.Event): Mono<*> {
        require(event is MessageCreateEvent)

        return event.client.self
                .map { user -> user.id }
                .flatMap { id ->
                    if (event.message.content == "<@!${id.asLong()}>") {
                        event.guild
                                .flatMap { guild ->
                                    val obj = GuildFactory.getOrCreate(guild.id.asLong())

                                    event.sendMessage("mention.default", guild.name, obj.prefix)
                                }
                    } else Mono.empty<Void>()
                }
    }
}