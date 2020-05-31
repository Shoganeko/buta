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

val VOLUME_COMMAND = Command(CommandConfig("volume")) {
    if (args.size != 1 && args[0].toIntOrNull() != null)
        return@Command event.sendMessage(container, "no-args").then()

    audioCommand(
            event,
            volume(event, AudioManager.getGuildMusicManager(event.guildId.get()), args[0].toIntOrNull()),
            false,
            sendMessage("not-in-channel").then(),
            sendMessage("not-in-buta-channel").then()
    )
}

/**
 * Set volume of [guild] using [e]
 */
private fun CommandContext.volume(e: MessageCreateEvent, guild: GuildMusicManager, volume: Int?): Mono<Void> {
    if (volume == null || volume > 100 || 0 > volume)
        return e.message.channel
                .flatMap { ch ->
                    ch.createEmbed { embed -> container.getEmbed("invalid-volume").applyEmbed(embed, e.message.author.get()) }
                }
                .then()

    return e.message.channel
            .doOnNext { guild.player.volume = volume }
            .flatMap { ch ->
                ch.createEmbed { embed ->
                    container.getEmbed("set-volume").applyEmbed(embed, e.message.author.get(),
                            hashMapOf("desc" to arrayListOf("$volume"))
                    )
                }
            }
            .then()
}