package dev.shog.buta.handle.audio

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import discord4j.rest.util.Snowflake
import java.util.concurrent.ConcurrentHashMap

object AudioManager {
    val playerManager = DefaultAudioPlayerManager()
    private val musicManagers = ConcurrentHashMap<Snowflake, GuildMusicManager>()

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        playerManager.registerSourceManager(YoutubeAudioSourceManager(true))
    }

    fun getGuildMusicManager(snowflake: Snowflake): GuildMusicManager {
        if (musicManagers[snowflake] == null) {
            val musicManager = GuildMusicManager(playerManager)
            musicManagers[snowflake] = musicManager
        }

        return musicManagers[snowflake]!!
    }
}