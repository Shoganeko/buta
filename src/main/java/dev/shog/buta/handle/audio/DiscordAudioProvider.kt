package dev.shog.buta.handle.audio

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import discord4j.voice.AudioProvider
import java.nio.ByteBuffer

/**
 * Provides audio to discord.
 */
class DiscordAudioProvider(private val player: AudioPlayer) : AudioProvider(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize())) {
    /**
     * I don't even know
     */
    private val frame = MutableAudioFrame()

    init {
        frame.setBuffer(buffer)
    }

    /**
     * Try to provide to Discord.
     */
    override fun provide(): Boolean {
        return try {
            val didProvide = player.provide(frame)
            if (didProvide) buffer.flip()

            didProvide
        } catch (ex: Exception) {
            false
        }
    }
}