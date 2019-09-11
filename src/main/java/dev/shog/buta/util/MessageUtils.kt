package dev.shog.buta.util

import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateSpec
import reactor.core.publisher.Flux
import java.awt.Color
import java.time.Instant
import kotlin.random.Random

/**
 * Updates [EmbedCreateSpec] with default values.
 */
fun EmbedCreateSpec.updateDefault(color: Color = Color(Random.nextInt(), Random.nextInt(), Random.nextInt())): EmbedCreateSpec {
    setColor(color)
    setTimestamp(Instant.now())

    return this
}

/**
 * Updates [EmbedCreateSpec] with proper footer, and avatar url, and applies [updateDefault].
 */
fun EmbedCreateSpec.update(user: User, color: Color = Color(96, 185, 233)): EmbedCreateSpec {
    setFooter("Requested by ${user.username}", user.avatarUrl)

    return updateDefault(color)
}

/**
 * Uses a [pre]set message and replaces with [replaceable]. Inserts a prefix into each argument if [incPrefix] is true.
 */
fun preset(pre: Pre, vararg replaceable: String, incPrefix: Boolean): String {
    if (pre.replaceable != replaceable.size)
        return pre.defaultMessage

    var message = pre.message
    for (i in 0 until pre.replaceable) {
        message = message.replaceFirst("<>", if (incPrefix) { "!" + replaceable[i] } else { replaceable[i] })
    }

    return message
}

/**
 * A preset message. The [message], and the amount of things that should be replaced.
 */
enum class Pre(internal val message: String, internal val replaceable: Int, internal val defaultMessage: String) {
    INV_ARGS("Invalid Usage!\nProper Usage: `<>`", 1, "Invalid Usage!"),
    NO_PERM("No Permission!", 0, "No Permission!")
}