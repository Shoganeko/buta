package dev.shog.buta.events

import dev.shog.buta.CLIENT
import dev.shog.buta.LOGGER
import dev.shog.buta.events.obj.Event
import dev.shog.buta.handle.PostgreSql
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
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
    val presences = arrayListOf(Presence.doNotDisturb())

    /**
     * Gets presences and adds them to [presences].
     */
    fun updatePresences() {
        synchronized(presences) {
            presences.clear()
        }

        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT * FROM buta.presences")
                .executeQuery()

        while (rs.next()) {
            val status = rs.getString("statusText")

            val activity = when (rs.getInt("activityType")) {
                1 -> Activity.playing(status)
                2 -> Activity.watching(status)
                3 -> Activity.listening(status)

                else -> Activity.playing(status)
            }

            synchronized(presences) {
                presences.add(when (rs.getInt("statusType")) {
                    1 -> Presence.online(activity)
                    2 -> Presence.invisible()
                    3 -> Presence.idle(activity)
                    4 -> Presence.doNotDisturb(activity)

                    else -> Presence.invisible()
                })
            }
        }
    }

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
    fun update(client: GatewayDiscordClient): Mono<Void> =
            presences
                    .random()
                    .toMono()
                    .doOnNext { LOGGER.info("Updating presence to ${it.status()}") }
                    .flatMap { client.updatePresence(it) }

    override fun invoke(event: discord4j.core.event.domain.Event): Mono<Void> {
        require(event is ReadyEvent)

        updatePresences()
        updateTimer(event.client)

        return update(event.client)
    }

    /**
     * Every time [updateTimer] activates.
     */
    private val TIMER_UPDATE_EVERY = TimeUnit.MINUTES.toMillis(5)
}
