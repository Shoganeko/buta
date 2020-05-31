package dev.shog.buta.commands.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.obj.CommandContext
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.util.*
import dev.shog.lib.util.fancyDate
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

val PLAY_COMMAND = Command(CommandConfig("play")) {
    if (args.isEmpty())
        return@Command event.sendMessage(container, "no-args").then()

    val identifier = args.joinToString(" ").trim()

    audioCommand(
            event,
            play(event, AudioManager.getGuildMusicManager(event.guildId.get()), identifier),
            true,
            sendMessage("not-in-channel"),
            sendMessage("not-in-buta-channel")
    )
}

/**
 * Load a track on [guild] using [identifier] and [e].
 */
private fun CommandContext.play(e: MessageCreateEvent, guild: GuildMusicManager, identifier: String): Mono<Void> =
        e.message.channel
                .doOnNext {
                    AudioManager.playerManager.loadItem("ytsearch:$identifier", object : AudioLoadResultHandler {
                        override fun playlistLoaded(playlist: AudioPlaylist?) {
                            if (playlist == null || playlist.tracks.isEmpty()) {
                                noMatches()
                                return
                            }

                            val track = playlist.tracks.first()

                            queueTrack(guild, e, track)
                        }

                        override fun noMatches() {
                            sendMessage("issue-loading", identifier).subscribe()
                        }

                        override fun trackLoaded(track: AudioTrack?) {
                            queueTrack(guild, e, track!!)
                        }

                        override fun loadFailed(exception: FriendlyException?) {
                            sendMessage("issue-loading").subscribe()
                        }
                    })
                }
                .then()

/**
 * Load a track for [guild] using [e] and [audioTrack].
 */
private fun CommandContext.queueTrack(guild: GuildMusicManager, e: MessageCreateEvent, audioTrack: AudioTrack) {
    guild.scheduler.queue(audioTrack)

    e.message.channel
            .doOnNext { ch -> guild.requestChannel = ch }
            .flatMap { ch ->
                ch.createEmbed { spec ->
                    container.getEmbed("queued-song").applyEmbed(spec, e.message.author.get(),
                            hashMapOf(
                                    "title" to audioTrack.info.title.ar(),
                                    "url" to audioTrack.info.uri.ar()
                            ),
                            hashMapOf(
                                    "length" to FieldReplacement(null, arrayListOf(audioTrack.info.length.fancyDate())),
                                    "author" to FieldReplacement(null, arrayListOf(audioTrack.info.author)),
                                    "time-to-play" to FieldReplacement(null, arrayListOf(determineTime(guild, audioTrack))),
                                    "queue-pos" to FieldReplacement(null, (guild.scheduler.getTracks().size + 1).ar())
                            )
                    )
                }
                        .flatMap { e.sendPlainText(audioTrack.info.uri) }
            }
            .subscribe()
}

/**
 * Determine the time to complete all music.
 */
private fun determineTime(guild: GuildMusicManager, new: AudioTrack): String {
    val trackLengths = guild.scheduler.getTracks()
            .asSequence()
            .map { track -> track.info.length }.sum()

    val total = if (new != guild.player.playingTrack)
        guild.player.playingTrack.info.length
    else 0 + trackLengths

    return if (total == 0L)
        "Now"
    else total.fancyDate()
}