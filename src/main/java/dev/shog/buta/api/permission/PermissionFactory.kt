package dev.shog.buta.api.permission

import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.core.entity.Member

object PermissionFactory {
    /**
     * The user has permission no matter what.
     */
    fun hasPermission() =
            object : Permable() {
                override suspend fun hasPermission(member: Member): Boolean = true
            }

    /**
     * User has [permissions]
     */
    fun hasPermission(vararg permissions: Permission): Permable =
            object : Permable() {
                override suspend fun hasPermission(member: Member): Boolean =
                        permissions.any { perm -> !member.getPermissions().contains(perm) }
            }
}