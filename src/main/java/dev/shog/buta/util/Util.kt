package dev.shog.buta.util

import dev.shog.buta.events.GuildJoinEvent
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