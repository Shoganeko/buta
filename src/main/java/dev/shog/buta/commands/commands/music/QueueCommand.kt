package dev.shog.buta.commands.commands.music

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.obj.msg.MessageHandler
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.handle.obj.getField
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.ar
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.fancyDate
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class QueueCommand : Command(CommandConfig(
        "pause",
        "Pause music.",
        Category.MUSIC,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return audioCommand(
                e,
                queue(e, AudioManager.getGuildMusicManager(e.guildId.get())),
                false,
                e.sendMessage(container, "not-in-channel").then(),
                e.sendMessage(container, "not-in-buta-channel").then()
        )
    }

    /**
     * Get a queue from [guild] using [e]
     */
    private fun queue(e: MessageCreateEvent, guild: GuildMusicManager): Mono<Void> {
        return e.message.channel
                .flatMap { ch ->
                    ch.createEmbed { embed ->
                        container.getEmbed("queue").applyEmbed(
                                embed,
                                e.message.author.get(),
                                hashMapOf("desc" to arrayListOf(guild.player.playingTrack?.info?.title ?: "Nothing")),
                                hashMapOf()
                        )

                        val size = guild.scheduler.getTracks().size
                        val goTo = if (size > 9) 9 else size

                        val field = container.getEmbed("song").getField()
                        for (i in 0 until goTo) {
                            val obj = guild.scheduler.getTracks()[i]

                            embed.addField(
                                    MessageHandler.formatText(field.title, i.ar()),
                                    MessageHandler.formatText(obj.info.title, obj.info.length.fancyDate().ar()),
                                    true
                            )
                        }
                    }
                }
                .then()
    }
}