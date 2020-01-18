package dev.shog.buta.util

import dev.shog.buta.APP
import dev.shog.buta.EN_US
import dev.shog.buta.LOGGER
import dev.shog.lib.util.toSuccessfulFailed
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.`object`.util.Permission
import kong.unirest.HttpResponse
import org.slf4j.Logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * Get an array of [T]
 */
fun <T> T.ar(): ArrayList<String> =
        arrayListOf(this.toString())

/**
 * Format [str] with [args]
 */
fun formatTextArray(str: String, args: Collection<*>): String {
    var newString = str

    args.forEachIndexed { i, arg ->
        if (newString.contains("{$i}"))
            newString = newString.replace("{$i}", arg.toString())
    }

    return newString
}

/**
 * Gets [TextChannel] from a [guild] that self has permissions to.
 */
fun getChannelsWithPermission(guild: Guild): Flux<TextChannel> {
    return guild.channels
            .ofType(TextChannel::class.java)
            .filterWhen { ch ->
                ch.getEffectivePermissions(ch.client.selfId.get())
                        .map { chl ->
                            chl.contains(Permission.SEND_MESSAGES)
                        }
            }
}

/**
 * Gets the type of
 */
fun getType(any: Any): Mono<String> =
        Flux.fromIterable(Types.values().toList())
                .filter { o -> o.name.toLowerCase() == any::class.java.simpleName.toLowerCase() }
                .collectList()
                .flatMap { o ->
                    if (o.size != 1)
                        Mono.empty()
                    else Mono.just(o[0].name.toLowerCase())
                }

/**
 * The different types for the API update object method.
 */
internal enum class Types {
    DOUBLE, STRING, INT, LONG
}

/**
 * Log [message] to [LOGGER].
 */
fun <T> Mono<T>.info(func: (T) -> String): Mono<T> =
        doOnNext { LOGGER.info(func.invoke(it)) }

/**
 * Format using [formatTextArray].
 */
fun String.form(vararg args: Any?): String =
        formatTextArray(this, args.toList())

fun <T : HttpResponse<*>> Mono<T>.logRequest(method: String, url: String): Mono<T> =
        info { resp -> "${resp.isSuccess.toSuccessfulFailed().capitalize()} -> $method $url : ${resp.status} : ${resp.body}\n" }

/**
 * Format using [formatTextArray].
 */
fun String.formArray(args: ArrayList<*>): String =
        formatTextArray(this, args)

/**
 * Get error by [err].
 */
fun getError(err: String): String =
        EN_US.get().getJSONObject("error").getString(err)