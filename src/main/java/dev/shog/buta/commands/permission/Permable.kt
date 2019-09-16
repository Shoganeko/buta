package dev.shog.buta.commands.permission

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

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
            Mono.just(e.guildId.isPresent)
                    .flatMap {
                        when {
                            it && e.member.isPresent -> Mono.just(e.member.get())
                            it && e.message.author.isPresent -> Mono.just(e.message.author.get())
                            else -> Mono.empty()
                        }
                    }
                    .flatMap { u -> hasPermission(u) }
                    .switchIfEmpty(Mono.just(false))
}