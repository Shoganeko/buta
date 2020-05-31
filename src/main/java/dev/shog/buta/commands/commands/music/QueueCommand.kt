package dev.shog.buta.commands.commands.music

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.obj.CommandContext
import dev.shog.buta.api.obj.msg.MessageHandler
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.handle.obj.getField
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.ar
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.fancyDate
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

val QUEUE_COMMAND = Command(CommandConfig("queue")) {
    audioCommand(
            event,
            queue(event, AudioManager.getGuildMusicManager(event.guildId.get())),
            false,
            sendMessage("not-in-channel"),
            sendMessage("not-in-buta-channel")
    )
}

/**
 * Get a queue from [guild] using [e]
 */
private fun CommandContext.queue(e: MessageCreateEvent, guild: GuildMusicManager): Mono<Void> {
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