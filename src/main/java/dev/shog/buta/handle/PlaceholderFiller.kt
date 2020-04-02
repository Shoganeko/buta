package dev.shog.buta.handle

import dev.shog.buta.util.orElse
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Fill a string with the proper variables.
 */
object PlaceholderFiller {
    /**
     * Fill [text] with placeholders from [event].
     */
    fun fillText(text: String, event: MessageCreateEvent): Mono<String> {
        val user = event.message.author.get()

        return text
                .replace("{user}", user.username)
                .toMono()
                .flatMap { str ->
                    event.guild
                            .map { guild ->
                                str.replace("{guild-name}", guild.name) // This should be {guild}, but then the guild class would misalign :(
                                        .replace("{guild-size}", guild.memberCount.orElse(-1).toString())
                            }
                }
    }
}