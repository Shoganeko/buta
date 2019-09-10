package dev.shog.buta.util

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