package dev.shog.buta.util

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import reactor.core.publisher.Mono
import java.awt.Color
import java.time.Instant
import kotlin.random.Random

/**
 * Updates [EmbedCreateSpec] with default values.
 */
fun EmbedCreateSpec.updateDefault(color: Color = Color(Random.nextInt(), Random.nextInt(), Random.nextInt())): EmbedCreateSpec {
    setColor(color)
    setTimestamp(Instant.now())

    return this
}

/**
 * Updates [EmbedCreateSpec] with proper footer, and avatar url, and applies [updateDefault].
 */
fun EmbedCreateSpec.update(user: User, color: Color = Color(96, 185, 233)): EmbedCreateSpec {
    setFooter("Requested by ${user.username}", user.avatarUrl)

    return updateDefault(color)
}

/**
 * Send a simple text message in the channel where [MessageCreateEvent] was created.
 */
fun MessageCreateEvent.sendMessage(msg: String): Mono<Message> =
        message.channel
                .flatMap { ch -> ch.createMessage(msg) }