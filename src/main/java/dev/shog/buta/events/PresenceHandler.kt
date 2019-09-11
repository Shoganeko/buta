package dev.shog.buta.events

import dev.shog.buta.CLIENT
import dev.shog.buta.LOGGER
import dev.shog.buta.events.obj.Event
import discord4j.core.`object`.data.stored.ActivityBean
import discord4j.core.`object`.data.stored.PresenceBean
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.gateway.json.response.PresenceResponse
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

/**
 * When [ReadyEvent] is invoked, the PresenceHandler will be initiated.
 */
object PresenceHandler: Event() {
    /**
     * The presences.
     */
    private val presences = ArrayList<Presence>()

    /**
     * Gets presences and adds them to [presences].
     */
    private fun updatePresences() {
        // TODO get presences from database
        val newPresences = arrayListOf(Presence.online(Activity.playing("Poggers")))

        presences.clear()
        presences.addAll(newPresences)
    }

    /**
     * Gets a random presence from [presences] and updates [CLIENT].
     */
    private fun updateTimer() {
        Timer().schedule(timerTask {
            val rand = presences.random()

            LOGGER.debug("Updating presence to ${rand.activity.get()} ${rand.status.value}")

            while (CLIENT == null) { }

            CLIENT?.updatePresence(rand)?.subscribe()
        }, 0, TIMER_UPDATE_EVERY)
    }

    override fun invoke(event: discord4j.core.event.domain.Event) {
        require(event is ReadyEvent)

        updatePresences()
        updateTimer()
    }

    /**
     * Every time [updateTimer] activates.
     */
    private const val TIMER_UPDATE_EVERY = 1000L*60*5 // 5 minutes
}
