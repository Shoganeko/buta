package dev.shog.buta.events.obj

import discord4j.core.event.domain.Event

/**
 * A event.
 */
abstract class Event {
    /**
     * When that event is invoked.
     */
    abstract fun invoke(event: Event)
}