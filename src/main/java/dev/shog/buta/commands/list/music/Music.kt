package dev.shog.buta.commands.commands.music

import dev.shog.buta.APP
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.util.sendMessage
import dev.shog.buta.util.sendPlainText
import dev.shog.lib.util.logDiscord
import discord4j.core.event.domain.message.MessageCreateEvent
import org.json.JSONObject
import reactor.core.publisher.Mono

/**
 * An audio command. This allows different actions to be executed depending on the situation.
 *
 * @param e The message event.
 * @param inVoice What to do when the user is in the right voice
 * @param notInVoice What to do when the user isn't in voice
 * @param shouldJoinWhenNotEntered If the bot should join the user voice channel.
 * @param notInRightVoice What to do when the user isn't in the same voice channel.
 */
fun audioCommand(
        e: MessageCreateEvent,
        inVoice: Mono<*>,
        shouldJoinWhenNotEntered: Boolean,
        notInVoice: Mono<*>,
        notInRightVoice: Mono<*>,
        notEntered: Mono<*> = e.sendPlainText("I'm not in a voice channel!")
): Mono<*> {
    val voiceChannel = e.member.get()
            .voiceState
            .flatMap { vc -> vc.channel }

    val botChannel = e.client.self
            .flatMap { user -> user.asMember(e.guildId.get()) }
            .flatMap { mem -> mem.voiceState }
            .flatMap { vc -> vc.channel }

    return voiceChannel
            .hasElement()
            .flatMap { inChannel ->
                if (inChannel) {
                    val guild = AudioManager.getGuildMusicManager(e.guildId.get())

                    botChannel
                            .hasElement()
                            .flatMap { botInChannel ->
                                if (botInChannel)
                                    Mono.zip(voiceChannel, botChannel)
                                            .map { ch -> ch.t1 == ch.t2 }
                                            .flatMap { sameChannel -> if (!sameChannel) notInRightVoice else inVoice }
                                else voiceChannel
                                        .flatMap { vc ->
                                            if (shouldJoinWhenNotEntered)
                                                vc
                                                        .join { spec -> spec.setProvider(guild.provider) }
                                                        .doOnNext { join -> guild.connection = join }
                                                        .flatMap { inVoice }
                                            else notEntered
                                        }
                            }
                } else notInVoice
            }
}