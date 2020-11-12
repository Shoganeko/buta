package dev.shog.buta.api.permission

import com.gitlab.kordlib.core.entity.Member

/**
 * A permission handle, from [PermissionFactory].
 */
abstract class Permable {
    /**
     * If a [member] has permission.
     */
    abstract suspend fun hasPermission(member: Member?): Boolean
}