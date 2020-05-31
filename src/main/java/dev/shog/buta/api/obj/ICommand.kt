package dev.shog.buta.api.obj

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/**
 * The main command interface.
 */
interface ICommand {
    /**
     * When the command is invoked by a user.
     */
    fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*>

    /**
     * When the help command is invoked by a user.
     */
    fun help(e: MessageCreateEvent): Mono<*>
}