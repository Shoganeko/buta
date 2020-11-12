package dev.shog.buta.handle

import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import dev.shog.buta.util.orElse

/**
 * Fill a string with the proper variables.
 */
object PlaceholderFiller {
    /**
     * Fill [text] with placeholders from [event].
     */
    suspend fun fillText(text: String, event: MessageCreateEvent): String? {
        val user = event.message.author?.asUser()
                ?: return null

        val guild = event.getGuild()
                ?: return null

        return text
                .replace("{user}", user.username)
                .replace("{guild-name}", guild.name) // This should be {guild}, but then the guild class would misalign :(
                .replace("{guild-size}", guild.memberCount.orElse(-1).toString())
    }
}