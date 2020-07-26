package dev.shog.buta.events

import dev.shog.buta.CLIENT
import dev.shog.buta.LOGGER
import dev.shog.buta.events.obj.Event
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.`object`.presence.Status
import discord4j.core.event.domain.lifecycle.ReadyEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

/**
 * When [ReadyEvent] is invoked, the PresenceHandler will be initiated.
 */
object PresenceHandler : Event {
    /**
     * The presences.
     */
    val presences = arrayListOf(Presence.doNotDisturb(Activity.playing("wit cho mama")))

    /**
     * Gets a random presence from [presences] and updates [CLIENT].
     */
    private fun updateTimer(client: GatewayDiscordClient) {
        Timer().schedule(timerTask {
            update(client).subscribe()
        }, 10000, TIMER_UPDATE_EVERY)
    }

    /**
     * Update Presence
     */
    fun update(client: GatewayDiscordClient): Mono<*> =
            presences
                    .random()
                    .toMono()
                    .doOnNext { LOGGER.info("Updating presence to ${it.status()}") }
                    .flatMap { client.updatePresence(it) }

    override fun invoke(event: discord4j.core.event.domain.Event): Mono<Void> {
        require(event is ReadyEvent)

        updateTimer(event.client)

        return update(event.client).then()
    }

    /**
     * Every time [updateTimer] activates.
     */
    private val TIMER_UPDATE_EVERY = TimeUnit.MINUTES.toMillis(5)
}
