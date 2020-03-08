package dev.shog.buta.handle.msg

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

fun MessageCreateEvent.sendMessageHandler(message: String, vararg form: String): Mono<Message> =
        this.message.channel
                .flatMap { ch -> ch.sendMessageHandler(message, *form) }

fun MessageChannel.sendMessageHandler(message: String, vararg form: String): Mono<Message> =
        createMessage(MessageHandler.getMessage(message, *form))