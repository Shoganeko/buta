package dev.shog.buta.commands.commands.music

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

val LEAVE_COMMAND = Command(CommandConfig("leave")) {
    audioCommand(
            event,
            disconnect(event, AudioManager.getGuildMusicManager(event.guildId.get())),
            false,
            sendMessage("not-in-channel"),
            sendMessage("not-in-buta-channel")
    )
}

/**
 * Disconnect from [guild] using [e]
 */
private fun disconnect(e: MessageCreateEvent, guild: GuildMusicManager): Mono<*> =
        guild.stop(true).toMono()