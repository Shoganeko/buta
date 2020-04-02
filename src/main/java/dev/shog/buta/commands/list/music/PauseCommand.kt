package dev.shog.buta.commands.commands.music

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class PauseCommand : Command(CommandConfig(
        "pause",
        "Pause music.",
        Category.MUSIC,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return audioCommand(
                e,
                pause(e, AudioManager.getGuildMusicManager(e.guildId.get())),
                false,
                e.sendMessage(container, "not-in-channel").then(),
                e.sendMessage(container, "not-in-buta-channel").then()
        )
    }

    /**
     * Pause [guild] using [e]
     */
    private fun pause(e: MessageCreateEvent, guild: GuildMusicManager): Mono<Void> {
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
}