package dev.shog.buta.commands.commands.music

import dev.shog.buta.APP
import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.logDiscord
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class LeaveCommand : Command(CommandConfig(
        "leave",
        "Leave the voice channel and stop playing music.",
        Category.MUSIC,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return audioCommand(
                e,
                disconnect(e, AudioManager.getGuildMusicManager(e.guildId.get())),
                false,
                e.sendMessage(container, "not-in-channel").then(),
                e.sendMessage(container, "not-in-buta-channel").then()
        )
    }

    /**
     * Disconnect from [guild] using [e]
     */
    private fun disconnect(e: MessageCreateEvent, guild: GuildMusicManager): Mono<*> =
            e.message.channel
                    .doOnNext { ch -> guild.requestChannel = ch }
                    .doOnNext {
                        try {
                            guild.connection?.disconnect() // Message is handled in VoiceUpdate event
                        } catch (ex: Exception) {
                            ex.logDiscord(APP)
                        }
                    }
}