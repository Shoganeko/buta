package dev.shog.buta.api.permission

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * A permission handle, from [PermissionFactory].
 */
abstract class Permable {
    /**
     * If a [member] has permission. This is in a guild environment.
     */
    abstract fun hasPermission(member: Member): Mono<Boolean>

    /**
     * If a [user] has permission. This is in a PM environment.
     */
    abstract fun hasPermission(user: User): Mono<Boolean>

    /**
     * Checks a [e] has permission.
     */
    fun check(e: MessageCreateEvent): Mono<Boolean> =
            when {
                e.member.isPresent -> hasPermission(e.member.get())
                e.message.author.isPresent -> hasPermission(e.message.author.get())
                else -> false.toMono()
            }
}