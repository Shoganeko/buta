package dev.shog.buta.handle

import dev.shog.buta.EN_US
import dev.shog.buta.commands.api.Api
import dev.shog.buta.commands.api.obj.Guild
import dev.shog.buta.util.info
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.`object`.util.Permission
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Swear filter stuff.
 */
object SwearFilter {
    private val SWEARS: Flux<String> = Api.getSwears().cache()

    /**
     * If a message contains a swear.
     *
     * @param guild The guild the message is from
     * @param message The message.
     * @param messageEvent The event where [message] was sent.
     */
    fun hasSwears(guild: Guild, message: String, messageEvent: MessageCreateEvent): Mono<Boolean> =
            if (guild.swearFilter.first == true) {
                SWEARS
                        .filter { swear -> message.contains(swear) }
                        .collectList()
                        .flatMap { list ->
                            if (list.isNotEmpty() || isAss(message)) {
                                messageEvent.message.channel
                                        .flatMap { ch ->
                                            PlaceholderFiller.fillText(guild.swearFilter.second
                                                    ?: EN_US.getEntry("default.swear-filter"), messageEvent)
                                                    .flatMap { msg -> ch.createMessage(msg) }
                                        }
                                        .filterWhen {
                                            messageEvent.message.channel
                                                    .ofType(TextChannel::class.java)
                                                    .zipWith(messageEvent.client.self)
                                                    .flatMap { ch -> ch.t1.getEffectivePermissions(ch.t2.id) }
                                                    .map { perms -> perms.contains(Permission.MANAGE_MESSAGES) }
                                        }
                                        .flatMap { messageEvent.message.delete() }
                                        .map { true }
                            } else false.toMono()
                        }
                        .info { it.toString() }
            } else false.toMono()

    /**
     * If any of strings in [message] contains exactly `ass`.
     */
    private fun isAss(message: String) =
            message.split(" ").asSequence()
                    .map { msg -> msg.replace(Regex("[^A-Za-z0-9]"), ""); }
                    .any { msg -> msg.toLowerCase() == "ass" || msg.toLowerCase() == getAssByLength(msg) }

    /**
     * Get a variable length ass.
     */
    private fun getAssByLength(message: String): String {
        val len = message.length - 1
        var msg = "a"

        (1..len)
                .forEach { _ -> msg += "s" }

        return msg
    }
}