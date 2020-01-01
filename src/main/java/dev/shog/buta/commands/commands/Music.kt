package dev.shog.buta.commands.commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.shog.buta.LOGGER
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.handle.obj.getField
import dev.shog.buta.util.*
import discord4j.core.event.domain.message.MessageCreateEvent
import org.json.JSONObject
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.util.stream.Collectors

/**
 * An audio command. This allows different actions to be executed depending on the situation.
 *
 * @param e The message event.
 * @param inVoice What to do when the user is in the right voice
 * @param notInVoice What to do when the user isn't in voice
 * @param shouldJoinWhenNotEntered If the bot should join the user voice channel.
 * @param notInRightVoice What to do when the user isn't in the same voice channel.
 */
private fun audioCommand(
        e: MessageCreateEvent,
        inVoice: Mono<Void>,
        shouldJoinWhenNotEntered: Boolean,
        notInVoice: Mono<Void>,
        notInRightVoice: Mono<Void>,
        notEntered: Mono<Void> = e.sendMessage("I'm not playing music!").then()
): Mono<Void> {
    val voiceChannel = e.member.get()
            .voiceState
            .flatMap { vc -> vc.channel }

    val botChannel = e.client.self
            .flatMap { user -> user.asMember(e.guildId.get()) }
            .flatMap { mem -> mem.voiceState }
            .flatMap { vc -> vc.channel }

    return voiceChannel
            .hasElement()
            .flatMap { inChannel ->
                if (inChannel) {
                    val guild = AudioManager.getGuildMusicManager(e.guildId.get())

                    botChannel
                            .hasElement()
                            .flatMap { botInChannel ->
                                if (botInChannel)
                                    Mono.zip(voiceChannel, botChannel)
                                            .map { ch -> ch.t1 == ch.t2 }
                                            .flatMap { sameChannel -> if (!sameChannel) notInRightVoice else inVoice }
                                else voiceChannel
                                        .flatMap { vc ->
                                            if (shouldJoinWhenNotEntered)
                                                vc
                                                        .join { spec -> spec.setProvider(guild.provider) }
                                                        .doOnNext { join -> guild.connection = join }
                                                        .flatMap { inVoice }
                                            else notEntered
                                        }
                            }
                } else notInVoice
            }
}

/**
 * Pause/resume music
 */
val MUSIC_PAUSE = Command("pause", Categories.MUSIC) { e, args, lang ->
    audioCommand(
            e,
            pause(e, AudioManager.getGuildMusicManager(e.guildId.get()), lang),
            false,
            e.sendMessage(lang.getString("not-in-channel")).then(),
            e.sendMessage(lang.getString("not-in-buta-channel")).then()
    )
}.build().add()

/**
 * Adjust volume
 */
val MUSIC_VOLUME = Command("volume", Categories.MUSIC) { e, args, lang ->
    if (args.size != 1 && args[0].toIntOrNull() != null)
        return@Command e.sendMessage(lang.getString("no-args")).then()

    audioCommand(
            e,
            volume(e, AudioManager.getGuildMusicManager(e.guildId.get()), args[0].toIntOrNull(), lang),
            false,
            e.sendMessage(lang.getString("not-in-channel")).then(),
            e.sendMessage(lang.getString("not-in-buta-channel")).then()
    )
}.build().add()

/**
 * Play music
 */
val MUSIC_PLAY = Command("play", Categories.MUSIC) { e, args, lang ->
    if (args.isEmpty())
        return@Command e.sendMessage(lang.getString("no-args")).then()

    val identifier = args.stream().collect(Collectors.joining(" ")).trim()

    audioCommand(
            e,
            play(e, AudioManager.getGuildMusicManager(e.guildId.get()), identifier, lang),
            true,
            e.sendMessage(lang.getString("not-in-channel")).then(),
            e.sendMessage(lang.getString("not-in-buta-channel")).then()
    )
}.build().add()

/**
 * Skip music
 */
val MUSIC_SKIP = Command("skip", Categories.MUSIC) { e, _, lang ->
    audioCommand(
            e,
            skip(e, AudioManager.getGuildMusicManager(e.guildId.get()), lang),
            false,
            e.sendMessage(lang.getString("not-in-channel")).then(),
            e.sendMessage(lang.getString("not-in-buta-channel")).then()
    )
}.build().add()

/**
 * Leave
 */
val MUSIC_LEAVE = Command("leave", Categories.MUSIC) { e, _, lang ->
    audioCommand(
            e,
            disconnect(e, AudioManager.getGuildMusicManager(e.guildId.get()), lang),
            false,
            e.sendMessage(lang.getString("not-in-channel")).then(),
            e.sendMessage(lang.getString("not-in-buta-channel")).then()
    )
}.build().add()

/**
 * View queue
 */
val MUSIC_QUEUE = Command("queue", Categories.MUSIC) { e, _, lang ->
    audioCommand(
            e,
            queue(e, AudioManager.getGuildMusicManager(e.guildId.get()), lang),
            false,
            e.sendMessage(lang.getString("not-in-channel")).then(),
            e.sendMessage(lang.getString("not-in-buta-channel")).then()
    )
}.build().add()

/**
 * Disconnect from [guild] using [e]
 */
private fun disconnect(e: MessageCreateEvent, guild: GuildMusicManager, lang: JSONObject): Mono<Void> {
    guild.stop()

    return e.sendMessage(lang.getString("disconnect")).then()
}

/**
 * Get a queue from [guild] using [e]
 */
private fun queue(e: MessageCreateEvent, guild: GuildMusicManager, lang: JSONObject): Mono<Void> {
    return e.message.channel
            .flatMap { ch ->
                ch.createEmbed { embed ->
                    lang.getJSONObject("queue").applyEmbed(
                            embed,
                            e.message.author.get(),
                            hashMapOf("desc" to arrayListOf(guild.player.playingTrack?.info?.title ?: "Nothing")),
                            hashMapOf()
                    )

                    val size = guild.scheduler.getTracks().size
                    val goTo = if (size > 9) 9 else size

                    val field = lang.getJSONObject("song").getField()
                    for (i in 0 until goTo) {
                        val obj = guild.scheduler.getTracks()[i]

                        embed.addField(
                                field.title.form(i),
                                field.desc.form(obj.info.title, obj.info.length.fancyDate()),
                                true
                        )
                    }
                }
            }
            .then()
}

/**
 * Pause [guild] using [e]
 */
private fun pause(e: MessageCreateEvent, guild: GuildMusicManager, lang: JSONObject): Mono<Void> {
    return e.message.channel
            .doOnNext { guild.player.isPaused = guild.player.isPaused.not() }
            .flatMap { ch ->
                ch.createEmbed { embed ->
                    lang.getJSONObject("pause-embed").applyEmbed(embed, e.message.author.get(),
                            hashMapOf("desc" to arrayListOf(if (guild.player.isPaused) "paused" else "resumed")),
                            hashMapOf()
                    )
                }
            }
            .then()
}

/**
 * Set volume of [guild] using [e]
 */
private fun volume(e: MessageCreateEvent, guild: GuildMusicManager, volume: Int?, lang: JSONObject): Mono<Void> {
    if (volume == null || volume > 100 || 0 > volume)
        return e.message.channel
                .flatMap { ch ->
                    ch.createEmbed { embed -> lang.getJSONObject("invalid-volume").applyEmbed(embed, e.message.author.get()) }
                }
                .then()

    return e.message.channel
            .doOnNext { guild.player.volume = volume }
            .flatMap { ch ->
                ch.createEmbed { embed ->
                    lang.getJSONObject("set-volume").applyEmbed(embed, e.message.author.get(),
                            hashMapOf("desc" to arrayListOf("$volume"))
                    )
                }
            }
            .then()
}

/**
 * Skip a track on [guild] using [e]
 */
private fun skip(e: MessageCreateEvent, guild: GuildMusicManager, lang: JSONObject): Mono<Void> {
    val nextTrack = guild.scheduler.nextTrack()

    return if (nextTrack == null)
        return e
                .sendMessage(lang.getString("no-next"))
                .doOnNext { guild.player.stopTrack() } // If there's a track playing
                .then()
    else nextTrack
            .toMono()
            .flatMap { tr ->
                e.message.channel
                        .flatMap { ch ->
                            ch.createEmbed { embed ->
                                embed.setTitle(lang.getString("new-title").form(tr.info.title))
                                embed.setDescription(tr.info.uri)
                            }
                        }
            }
            .then()
}

/**
 * Load a track on [guild] using [identifier] and [e].
 */
private fun play(e: MessageCreateEvent, guild: GuildMusicManager, identifier: String, lang: JSONObject): Mono<Void> =
        e.message.channel
                .doOnNext {
                    AudioManager.playerManager.loadItem("ytsearch:$identifier", object : AudioLoadResultHandler {
                        override fun playlistLoaded(playlist: AudioPlaylist?) {
                            if (playlist == null || playlist.tracks.isEmpty()) {
                                noMatches()
                                return
                            }

                            val track = playlist.tracks.first()

                            queueTrack(guild, e, track, lang)
                        }

                        override fun noMatches() {
                            e.sendMessage(lang.getString("no-matches").form(identifier))
                                    .subscribe()
                        }

                        override fun trackLoaded(track: AudioTrack?) {
                            queueTrack(guild, e, track!!, lang)
                        }

                        override fun loadFailed(exception: FriendlyException?) {
                            e.sendMessage(lang.getString("issue-loading"))
                                    .subscribe()
                        }
                    })
                }
                .then()

/**
 * Load a track for [guild] using [e] and [audioTrack].
 */
private fun queueTrack(guild: GuildMusicManager, e: MessageCreateEvent, audioTrack: AudioTrack, lang: JSONObject) {
    guild.scheduler.queue(audioTrack)

    e.message.channel
            .doOnNext { ch -> guild.requestChannel = ch }
            .flatMap { ch ->
                ch.createEmbed { spec ->
                    lang.getJSONObject("queued-song").applyEmbed(spec, e.message.author.get(),
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
                        .flatMap { e.sendMessage(audioTrack.info.uri) }
            }
            .subscribe()
}

/**
 * Determine the time to complete all music.
 */
private fun determineTime(guild: GuildMusicManager, new: AudioTrack): String {
    val trackLengths = guild.scheduler.getTracks()
            .stream()
            .map { track -> track.info.length }
            .collect(Collectors.summingLong(Long::toLong))

    val total = if (new != guild.player.playingTrack)
        guild.player.playingTrack.info.length
    else 0 + trackLengths

    return if (total == 0L)
        "Now"
    else total.fancyDate()
}