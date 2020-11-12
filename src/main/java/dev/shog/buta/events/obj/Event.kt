package dev.shog.buta.events.obj

import com.gitlab.kordlib.core.event.Event

/**
 * A event.
 */
interface Event {
    /**
     * When that event is invoked.
     */
    suspend fun invoke(event: Event)
}