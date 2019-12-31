package dev.shog.buta.util

import dev.shog.DiscordWebhookHandler
import dev.shog.buta.EN_US
import dev.shog.buta.PRODUCTION
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.`object`.util.Permission
import org.slf4j.Logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

/** Turns ms into a seconds, day and hours format */
fun Long.fancyDate(): String {
    var response = ""

    val seconds = this / 1000

    if (seconds <= 60) {
        // Assuming there's multiple seconds
        return "$seconds seconds"
    }

    val minutes = seconds / 60

    if (minutes < 60)
        return if (minutes > 1) "$minutes minutes ${seconds - minutes * 60} seconds" else "$minutes minute ${seconds - minutes * 60} seconds"

    val hours = minutes / 60
    val hoursMinutes = minutes - hours * 60

    if (hours < 24) {
        response += if (hours > 1) "$hours hours " else "$hours hour "
        response += if (hoursMinutes > 1) "$hoursMinutes minutes" else "$hoursMinutes minute"

        return response
    }

    val days = hours / 24
    val hoursDays = hours - days * 24

    if (days < 7) {
        response += if (days > 1) "$days days " else "$days day "
        response += if (hoursDays > 1) "$hoursDays hours" else "$hoursDays hour"

        return response
    }

    val weeks = days / 7
    val weekDays = days - weeks * 7

    response += if (weeks > 1) "$weeks weeks " else "$weeks week "
    response += if (weekDays > 1) "$weekDays days" else "$weekDays day"

    return response
}

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
fun Boolean.enabledDisabled(): String = enableDisable() + "d"

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

/**
 * If it's in production mode, return [prod] else [dev].
 */
fun <T : Any?> swap(prod: T, dev: T): T {
    if (prod == null || dev == null)
        throw Exception("Swap found null variable!")

    if (prod == "empty" || dev == "empty")
        throw Exception("Swap found empty variable!")

    return if (PRODUCTION) prod else dev
}

/**
 * A fatal [err].
 */
fun Logger.fatal(err: String): Mono<Void> =
        DiscordWebhookHandler.sendMessage("FATAL (@everyone) - $err")
                .doOnNext { this.error("FATAL - $err") }

/**
 * Format using [formatTextArray].
 */
fun String.form(vararg args: Any): String =
        formatTextArray(this, args.toList())

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