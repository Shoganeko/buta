package dev.shog.buta.api.obj.perms

import com.gitlab.kordlib.core.entity.Member
import dev.shog.buta.api.permission.Permable

object All : Permable() {
    override suspend fun hasPermission(member: Member?): Boolean = true
}