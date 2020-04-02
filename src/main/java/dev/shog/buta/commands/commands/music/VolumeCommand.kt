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

class VolumeCommand : Command(CommandConfig(
        "volume",
        "Change the volume on the music.",
        Category.MUSIC,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        if (args.size != 1 && args[0].toIntOrNull() != null)
            return e.sendMessage(container, "no-args").then()

        return audioCommand(
                e,
                volume(e, AudioManager.getGuildMusicManager(e.guildId.get()), args[0].toIntOrNull()),
                false,
                e.sendMessage(container, "not-in-channel").then(),
                e.sendMessage(container, "not-in-buta-channel").then()
        )
    }

    /**
     * Set volume of [guild] using [e]
     */
    private fun volume(e: MessageCreateEvent, guild: GuildMusicManager, volume: Int?): Mono<Void> {
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
}