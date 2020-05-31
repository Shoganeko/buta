package dev.shog.buta.api.permission

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import discord4j.rest.util.Permission
import reactor.core.publisher.Mono

object PermissionFactory {
    /**
     * If a user has the required permissions.
     */
    fun hasPermissions(guild: ArrayList<Permission>, pm: (User) -> (Boolean)): Permable =
            object : Permable() {
                override fun hasPermission(user: User): Mono<Boolean> =
                        Mono.just(pm.invoke(user))

                override fun hasPermission(member: Member): Mono<Boolean> =
                        member.basePermissions
                                .map { p -> p.containsAll(guild) }
            }

    /**
     * Invokes [guild] and [pm] to check if the user has permission.
     */
    fun hasPermissions(guild: (Member) -> (Boolean), pm: (User) -> (Boolean)): Permable =
            object : Permable() {
                override fun hasPermission(user: User): Mono<Boolean> =
                        Mono.just(pm.invoke(user))

                override fun hasPermission(member: Member): Mono<Boolean> =
                        Mono.just(guild.invoke(member))
            }

    /**
     * The user has permission no matter what.
     */
    fun hasPermission() =
            object : Permable() {
                override fun hasPermission(user: User): Mono<Boolean> = Mono.just(true)

                override fun hasPermission(member: Member): Mono<Boolean> = Mono.just(true)
            }

    /**
     * The user has permission in pms, but has to have [guild] permissions in guild.
     */
    fun hasPermission(vararg guild: Permission): Permable =
            object : Permable() {
                override fun hasPermission(user: User): Mono<Boolean> = Mono.just(true)

                override fun hasPermission(member: Member): Mono<Boolean> =
                        member.basePermissions
                                .map { p -> p.containsAll(guild.toList()) }
            }
}