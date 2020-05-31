package dev.shog.buta.commands.commands.music

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.obj.CommandContext
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

val SKIP_COMMAND = Command(CommandConfig("skip")) {
    audioCommand(
            event,
            skip(event, AudioManager.getGuildMusicManager(event.guildId.get())),
            false,
            sendMessage("not-in-channel"),
            sendMessage("not-in-buta-channel")
    )
}

/**
 * Skip a track on [guild] using [e]
 */
private fun CommandContext.skip(e: MessageCreateEvent, guild: GuildMusicManager): Mono<Void> {
    val nextTrack = guild.scheduler.nextTrack()

    return if (nextTrack == null)
        return sendMessage("no-next")
                .doOnNext { guild.player.stopTrack() } // If there's a track playing
                .then()
    else nextTrack
            .toMono()
            .flatMap { tr ->
                e.message.channel
                        .flatMap { ch ->
                            ch.createEmbed { embed ->
                                embed.setTitle(container.getMessage("new-title", tr.info.title))
                                embed.setDescription(tr.info.uri)
                            }
                        }
            }
            .then()
}