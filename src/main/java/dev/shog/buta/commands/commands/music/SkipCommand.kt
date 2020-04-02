package dev.shog.buta.commands.commands.music

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class SkipCommand : Command(CommandConfig(
        "skip",
        "Skip the currently playing song.",
        Category.MUSIC,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return audioCommand(
                e,
                skip(e, AudioManager.getGuildMusicManager(e.guildId.get())),
                false,
                e.sendMessage(container, "not-in-channel").then(),
                e.sendMessage(container, "not-in-buta-channel").then()
        )
    }

    /**
     * Skip a track on [guild] using [e]
     */
    private fun skip(e: MessageCreateEvent, guild: GuildMusicManager): Mono<Void> {
        val nextTrack = guild.scheduler.nextTrack()

        return if (nextTrack == null)
            return e
                    .sendMessage(container, "no-next")
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
}