package dev.shog.buta.util

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.`object`.util.Permission
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

/**
 * How old is
 */
fun howOld(long: Long) = System.currentTimeMillis() - long

/**
 * The date formatter.
 */
internal val FORMATTER = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.LONG)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())

/**
 * Formats a [Long] using [FORMATTER].
 */
fun Long.format(): String = FORMATTER.format(Instant.ofEpochMilli(this))

/**
 * Formats an [Instant] using [FORMATTER].
 */
fun Instant.format(): String = FORMATTER.format(this)

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
 * Turns true into "enable" and false into "disable".
 */
fun Boolean.enableDisable(): String = if (this) "enable" else "disable"

/**
 * Adds a d onto [enableDisable]
 */
fun Boolean.enabledDisabled(): String = enabledDisabled() + "d"

/**
 * Turns true into "yes" and false into "no".
 */
fun Boolean.yesNo(): String = if (this) "yes" else "no"

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