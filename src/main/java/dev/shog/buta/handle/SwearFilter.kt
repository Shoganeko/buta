package dev.shog.buta.handle

import dev.shog.buta.api.obj.Guild
import kong.unirest.Unirest
import org.json.JSONArray

/**
 * Swear filter stuff.
 */
object SwearFilter {
    private val SWEARS: MutableList<String> by lazy {
        val request = Unirest.get("https://raw.githubusercontent.com/Shoganeko/badwords/master/array.js")
                .asString()

        val body = request.body

        return@lazy JSONArray(body).toList()
                .map { it.toString() }
                .toMutableList()
    }

    /**
     * If a message contains a swear.
     *
     * @param guild The guild the message is from
     * @param message The message.
     * @param messageEvent The event where [message] was sent.
     */
    fun hasSwears(guild: Guild, message: String): Boolean {
        return if (guild.swearFilterOn) {
            SWEARS.any { swear ->
                if (message.contains(swear)) {
                    println("contains $swear")
                    true
                } else false
            } || isAss(message)
        } else
            false
    }

    /**
     * If any of strings in [message] contains exactly `ass`.
     */
    private fun isAss(message: String) =
            message.split(" ").asSequence()
                    .map { msg -> msg.replace(Regex("[^A-Za-z0-9]"), ""); }
                    .any { msg ->
                        msg.toLowerCase() == "ass" || (msg.length > 2 && msg.toLowerCase() == getAssByLength(msg))
                    }

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