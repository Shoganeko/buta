package dev.shog.buta.util

import dev.shog.buta.APP
import dev.shog.buta.LOGGER
import dev.shog.lib.util.logDiscord
import dev.shog.lib.util.toSuccessfulFailed
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.rest.util.Permission
import kong.unirest.HttpResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.awt.Color
import kotlin.random.Random

fun String.nullIfBlank(): String? =
        if (isBlank()) null else this

fun getRandomColor(): Color =
        Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

/**
 * Get an array of [T]
 */
fun <T> T.ar(): ArrayList<String> =
        arrayListOf(this.toString())

/**
 * Gets [TextChannel] from a [guild] that self has permissions to.
 */
fun getChannelsWithPermission(guild: Guild): Flux<TextChannel> {
    return guild.channels
            .ofType(TextChannel::class.java)
            .filterWhen { ch ->
                ch.client.selfId.flatMap { id ->
                    ch.getEffectivePermissions(id)
                            .map { chl ->
                                chl.contains(Permission.SEND_MESSAGES)
                                        && chl.contains(Permission.EMBED_LINKS)
                            }
                }
            }
}

/**
 * [this] ?: [t]
 */
fun <T : Any> T?.orElse(t: T): T =
        this ?: t

/**
 * Log [func] to [LOGGER].
 */
fun <T> Mono<T>.info(func: (T) -> String): Mono<T> =
        doOnNext { LOGGER.info(func.invoke(it)) }

fun <T : HttpResponse<*>> Mono<T>.logRequest(method: String, url: String): Mono<T> =
        info { resp ->
            ("Web Request (${resp.isSuccess.toSuccessfulFailed().capitalize()}): $method $url -> ${resp.status}" +
                    "\n\n${resp.body}").logDiscord(APP)

            "${resp.isSuccess.toSuccessfulFailed().capitalize()} -> $method $url : ${resp.status} : ${resp.body}"
        }