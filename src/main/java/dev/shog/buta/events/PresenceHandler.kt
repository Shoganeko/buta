package dev.shog.buta.events

import dev.shog.buta.CLIENT
import dev.shog.buta.LOGGER
import dev.shog.buta.events.obj.Event
import dev.shog.buta.handle.Mongo
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.`object`.presence.Status
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.discordjson.json.gateway.StatusUpdate
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
     * Presences from MongoDB
     */
    val presences by lazy {
        val presences = Mongo.getClient()
                .getDatabase("buta")
                .getCollection("presences")
                .find()

        val array = arrayListOf<StatusUpdate>()

        presences.forEach { doc ->
            val data = doc.getString("data")

            val activity = when (doc.getInteger("activity")) {
                2 -> Activity.listening(data)
                3 -> Activity.streaming(data, doc.getString("streamUrl"))
                4 -> Activity.watching(data)

                else -> Activity.playing(data)
            }

            val presence = when (doc.getInteger("presence")) {
                2 -> Presence.idle(activity)
                3 -> Presence.doNotDisturb(activity)
                4 -> Presence.invisible()
                else -> Presence.online(activity)
            }

            array.add(presence)
        }

        return@lazy array
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
