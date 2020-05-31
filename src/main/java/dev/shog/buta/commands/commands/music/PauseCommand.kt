package dev.shog.buta.commands.commands.music

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.obj.CommandContext
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

val PAUSE_COMMAND = Command(CommandConfig("pause")) {
    audioCommand(
            event,
            pause(event, AudioManager.getGuildMusicManager(event.guildId.get())),
            false,
            sendMessage("not-in-channel"),
            sendMessage("not-in-buta-channel")
    )
}

/**
 * Pause [guild] using [e]
 */
private fun CommandContext.pause(e: MessageCreateEvent, guild: GuildMusicManager): Mono<Void> {
    return e.message.channel
            .doOnNext { guild.player.isPaused = guild.player.isPaused.not() }
            .flatMap { ch ->
                ch.createEmbed { embed ->
                    container.getEmbed("pause-embed").applyEmbed(embed, e.message.author.get(),
                            hashMapOf("desc" to arrayListOf(if (guild.player.isPaused) "paused" else "resumed")),
                            hashMapOf()
                    )
                }
            }
            .then()
}