package dev.shog.buta.events.obj

import discord4j.core.event.domain.Event
import reactor.core.publisher.Mono

/**
 * A event.
 */
interface Event {
    /**
     * When that event is invoked.
     */
    fun invoke(event: Event): Mono<*>
}