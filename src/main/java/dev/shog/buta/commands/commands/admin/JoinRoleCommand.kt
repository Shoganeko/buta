package dev.shog.buta.commands.commands.admin

import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.toEnabledDisabled
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.rest.util.Permission
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Get a role name
 */
private fun getRole(long: Long?, guild: Mono<Guild>): Mono<String> {
    if (long == null || long == -1L)
        return "none".toMono()

    val role = guild
            .flatMap { g -> g.getRoleById(Snowflake.of(long)) }

    return role.hasElement()
            .flatMap { has -> if (has) role.map(Role::getName) else "none".toMono() }
}

val JOIN_ROLE_COMMAND = Command(CommandConfig("joinrole", PermissionFactory.hasPermission(Permission.ADMINISTRATOR))) {
    if (args.isEmpty()) {
        val guild = GuildFactory.getOrCreate(event.guildId.get().asLong())

        return@Command guild.toMono()
                .map { g -> g.joinRole }
                .flatMap { role ->
                    val name = getRole(role, event.guild)
                    val en = guild.isJoinRoleEnabled().toEnabledDisabled()

                    if (!guild.isJoinMessageEnabled())
                        event.sendMessage(container, "un-set", en, name)
                    else event.sendMessage(container, "on-set", en, name)
                }
                .then()
    }

    return@Command when (args.first().toLowerCase()) {
        "role" -> {
            args.removeAt(0)

            val built = args.joinToString(" ")

            event.guild
                    .flatMapMany { guild -> guild.roles }
                    .filter { role -> role.name.equals(built, true) }
                    .map { role -> role.id.asLong() }
                    .defaultIfEmpty(-1L)
                    .flatMap { roleId ->
                        if (roleId == -1L) {
                            sendMessage("invalid-role")
                        } else sendMessage("set", built.toLowerCase())
                                .doOnNext {
                                    GuildFactory.getOrCreate(event.guildId.get().asLong())
                                            .joinRole = roleId
                                }
                    }
                    .then()
        }

        "disable" -> {
            sendMessage("disabled")
                    .doOnNext { GuildFactory.getObject(event.guildId.get().asLong())?.joinRole = -1L }
                    .then()
        }

        else -> sendMessage("invalid-args")
    }
}