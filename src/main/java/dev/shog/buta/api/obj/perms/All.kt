package dev.shog.buta.api.obj.perms

import dev.shog.buta.api.permission.Permable
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import reactor.core.publisher.Mono

object All : Permable() {
    override fun hasPermission(member: Member): Mono<Boolean> = Mono.just(true)
    override fun hasPermission(user: User): Mono<Boolean> = Mono.just(true)
}