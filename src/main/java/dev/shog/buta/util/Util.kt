package dev.shog.buta.util

import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.entity.channel.TextChannel
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * Get null if a string is blank.
 */
fun String.nullIfBlank(): String? =
    if (isBlank()) null else this

/**
 * Add a footer to the embed [member].
 */
fun EmbedBuilder.addFooter(member: Member) {
    timestamp = Instant.now()

    footer {
        text = "Requested by ${member.username}#${member.discriminator}"
        icon = member.avatar.url
    }
}

/**
 * Add a footer from an [event].
 */
fun EmbedBuilder.addFooter(event: MessageCreateEvent) {
    addFooter(event.member ?: return)
}

/**
 * Get a user's username and discriminator.
 */
fun User.fullUsername(): String =
    "$username#$discriminator"

/**
 * Gets [TextChannel] from a [guild] that self has permissions to.
 */
suspend fun getChannelsWithPermission(guild: Guild): Flow<TextChannel> {
    return guild.channels
        .filter {
            val perms = it.getEffectivePermissions(guild.kord.selfId)

            it is TextChannel && perms.contains(Permission.SendMessages) && perms.contains(Permission.EmbedLinks)
        }
        .map { it as TextChannel }
}

/**
 * [this] ?: [t]
 */
fun <T : Any> T?.orElse(t: T): T =
    this ?: t