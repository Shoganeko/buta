package dev.shog.buta.commands.commands.admin

import dev.shog.buta.commands.api.factory.GuildFactory
import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.lib.transport.duo
import dev.shog.lib.util.toEnabledDisabled
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import discord4j.rest.util.Snowflake
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class JoinRoleCommand : Command(CommandConfig(
        name = "joinrole",
        desc = "Manage a role on join.",
        category = Category.ADMINISTRATOR,
        permable = PermissionFactory.hasPermission(Permission.ADMINISTRATOR)
)) {
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

    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        if (args.isEmpty()) {
            val guild = GuildFactory.getOrCreate(e.guildId.get().asLong())

            return guild.toMono()
                    .map { g -> g.joinRole }
                    .flatMap { role ->
                        val name = getRole(role, e.guild)
                        val en = guild.isJoinRoleEnabled().toEnabledDisabled()

                        if (!guild.isJoinMessageEnabled())
                            e.sendMessage(container, "un-set", en, name)
                        else e.sendMessage(container, "on-set", en, name)
                    }
                    .then()
        }

        return when (args.first().toLowerCase()) {
            "role" -> {
                args.removeAt(0)

                val built = args.joinToString(" ")

                e.guild
                        .flatMapMany { guild -> guild.roles }
                        .filter { role -> role.name.equals(built, true) }
                        .map { role -> role.id.asLong() }
                        .defaultIfEmpty(-1L)
                        .flatMap { roleId ->
                            if (roleId == -1L) {
                                e.sendMessage(container, "invalid-role")
                            } else e.sendMessage(container, "set", built.toLowerCase())
                                    .doOnNext {
                                        GuildFactory.getOrCreate(e.guildId.get().asLong())
                                                .joinRole = roleId
                                    }
                        }
                        .then()
            }

            "disable" -> {
                e.sendMessage(container, "disable")
                        .doOnNext { GuildFactory.getObject(e.guildId.get().asLong())?.joinRole = -1L }
                        .then()
            }

            else -> e.sendMessage(container, "invalid-args")
        }
    }
}