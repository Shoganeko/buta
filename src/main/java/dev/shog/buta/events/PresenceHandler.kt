package dev.shog.buta.events

import dev.shog.buta.CLIENT
import dev.shog.buta.LOGGER
import dev.shog.buta.commands.api.Api
import dev.shog.buta.events.obj.Event
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.lifecycle.ReadyEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*
import kotlin.concurrent.timerTask

/**
 * When [ReadyEvent] is invoked, the PresenceHandler will be initiated.
 */
object PresenceHandler : Event {
    /**
     * The presences.
     */
    val presences = arrayListOf(Presence.idle(Activity.playing("PepeLaugh")))

    /**
     * Gets presences and adds them to [presences].
     */
    fun updatePresences(): Mono<Void> =
            Api.getPresences()
                    .collectList()
                    .doOnNext { presences.clear() }
                    .doOnNext { pres -> presences.addAll(pres) }
                    .then()

    /**
     * Gets a random presence from [presences] and updates [CLIENT].
     */
    private fun updateTimer(client: GatewayDiscordClient) {
        Timer().schedule(timerTask { update(client).subscribe() }, 10000, TIMER_UPDATE_EVERY)
    }

    /**
     * Update Presence
     */
    fun update(client: GatewayDiscordClient): Mono<Void> =
            presences
                    .random()
                    .toMono()
                    .doOnNext { LOGGER.info("Updating presence to ${it.activity.get().name} ${it.status.value}") }
                    .flatMap { client.updatePresence(it) }

    override fun invoke(event: discord4j.core.event.domain.Event): Mono<Void> {
        require(event is ReadyEvent)

        updateTimer(event.client)
        return updatePresences()
    }

    /**
     * Every time [updateTimer] activates.
     */
    private const val TIMER_UPDATE_EVERY = 1000L * 60 * 5
}
