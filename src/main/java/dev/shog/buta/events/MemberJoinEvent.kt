package dev.shog.buta.events

import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.any
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.events.obj.Event

object MemberJoinEvent : Event {
    /**
     * Check if [member] has permission to properly hand out the join role.
     */
    private suspend fun hasPermission(member: Member): Boolean = member.getPermissions().contains(Permission.Administrator)

    override suspend fun invoke(event: com.gitlab.kordlib.core.event.Event) {
        require(event is MemberJoinEvent)

        val selfMember = event.kord
                .getSelf()
                .asMember(event.guildId)

        if (hasPermission(selfMember)) {
            val guild = GuildFactory.getOrCreate(event.guildId.longValue)

            if (guild.isJoinRoleEnabled() && guild.joinRole != -1L) {
                val role = event.guild.getRoleOrNull(Snowflake(guild.joinRole))

                if (role != null && selfMember.roles.any { selfRole -> selfRole.rawPosition > role.rawPosition }) {
                    event.member.addRole(role.id, "Automatically added on join.")
                }
            }
        }
    }
}