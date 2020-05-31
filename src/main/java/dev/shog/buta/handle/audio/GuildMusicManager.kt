package dev.shog.buta.handle.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.shog.buta.api.obj.msg.MessageHandler


import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.voice.VoiceConnection
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

/**
 * A guild music manager.
 * This manages a guild's music operations.
 */
class GuildMusicManager(manager: AudioPlayerManager) {
    private var disconnectTimer = Timer()
    private val checkInterval = TimeUnit.MINUTES.toMillis(20)

    /**
     * If a disconnect timer has been set.
     */
    private var hasQueuedTimer = true

    /**
     * The bot should disconnect if there's
     */
    private fun shouldDisconnect(): Boolean =
            scheduler.getTracks().isEmpty() && player.playingTrack == null

    /**
     * The actual player.
     */
    val player: AudioPlayer = manager.createPlayer()

    /**
     * The track scheduler. This allows for a queue of songs.
     */
    val scheduler: TrackScheduler = TrackScheduler(this)

    /**
     * This provides audio to Discord.
     */
    val provider: DiscordAudioProvider = DiscordAudioProvider(player)

    /**
     * The voice connection to the channel. This allows for later disconnecting.
     */
    var connection: VoiceConnection? = null

    /**
     * The voice channel Buta is in.
     */
    var voiceChannel: VoiceChannel? = null

    /**
     * The channel where music playing was requested.
     */
    var requestChannel: MessageChannel? = null

    /**
     * Stop playing, clear tracks, and disconnect.
     */
    fun stop(sendMessage: Boolean) {
        scheduler.clearTracks()
        player.stopTrack()

        disconnectTimer.cancel()
        disconnectTimer = Timer()
        hasQueuedTimer = false

        if (sendMessage)
            requestChannel
                    ?.createMessage(MessageHandler.getMessage("music.stop-playing"))
                    ?.subscribe()

        try {
            connection?.disconnect()?.subscribe()
        } catch (ex: Exception) { // I don't know why there's an error, but a try block fixes it :)
            ex.printStackTrace()
        }
    }

    /**
     * Cancel [disconnectTimer] and do [scheduleTimer]. This will reset the timer and account for activity.
     */
    fun rescheduleTimer() {
        disconnectTimer.cancel()
        disconnectTimer = Timer()
        scheduleTimer()
    }

    /**
     * Schedule the timer.
     */
    private fun scheduleTimer(): Mono<Unit> {
        hasQueuedTimer = true

        disconnectTimer.schedule(timerTask {
            hasQueuedTimer = false

            try {
                if (shouldDisconnect()) {
                    stop(true)
                } else scheduleTimer()
            } catch (ex: Exception) {
            }
        }, checkInterval)

        return Unit.toMono()
    }

    init {
        player.addListener(scheduler)
        scheduleTimer()
    }
}