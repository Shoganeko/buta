package dev.shog.buta.handle.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.shog.buta.commands.obj.msg.MessageHandler


import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.voice.VoiceConnection

/**
 * A guild music manager.
 * This manages a guild's music operations.
 */
class GuildMusicManager(manager: AudioPlayerManager) {
    /**
     * The actual player.
     */
    val player: AudioPlayer = manager.createPlayer()

    /**
     * The track scheduler. This allows for a queue of songs.
     */
    val scheduler: TrackScheduler = TrackScheduler(player)

    /**
     * This provides audio to Discord.
     */
    val provider: DiscordAudioProvider = DiscordAudioProvider(player)

    /**
     * The voice connection to the channel. This allows for later disconnecting.
     */
    var connection: VoiceConnection? = null

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

        if (sendMessage)
            requestChannel?.createMessage(MessageHandler.getMessage("music.stop-playing"))?.subscribe()
        try {
            connection?.disconnect()
        } catch (ex: Exception) { // I don't know why there's an error, but a try block fixes it :)
        }
    }

    init {
        player.addListener(scheduler)
    }
}