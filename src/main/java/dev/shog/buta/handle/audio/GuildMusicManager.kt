package dev.shog.buta.handle.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import discord4j.voice.VoiceConnection

class GuildMusicManager(manager: AudioPlayerManager) {
    val player: AudioPlayer = manager.createPlayer()
    val scheduler: TrackScheduler = TrackScheduler(player)
    val provider: DiscordAudioProvider = DiscordAudioProvider(player)
    var connection: VoiceConnection? = null

    fun stop() {
        scheduler.clearTracks()
        player.stopTrack()
        connection?.disconnect()
    }

    init {
        player.addListener(scheduler)
    }
}