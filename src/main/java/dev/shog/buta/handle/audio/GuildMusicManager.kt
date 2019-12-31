package dev.shog.buta.handle.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.shog.buta.EN_US
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.TextChannel
import discord4j.voice.VoiceConnection
import java.lang.Exception

class GuildMusicManager(manager: AudioPlayerManager) {
    val player: AudioPlayer = manager.createPlayer()
    val scheduler: TrackScheduler = TrackScheduler(player)
    val provider: DiscordAudioProvider = DiscordAudioProvider(player)
    var connection: VoiceConnection? = null
    var requestChannel: MessageChannel? = null

    fun stop() {
        scheduler.clearTracks()
        player.stopTrack()

        requestChannel
                ?.createMessage(EN_US.get().getJSONObject("music").getString("stop-playing"))
                ?.subscribe()

        try {
            connection?.disconnect()
        } catch (ex: Exception) { // I don't know why there's an error, but a try block fixes it :)
        }
    }

    init {
        player.addListener(scheduler)
    }
}