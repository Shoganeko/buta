package dev.shog.buta.commands.api.user

import dev.shog.buta.commands.api.API
import dev.shog.buta.commands.api.obj.DataHolder

/**
 * A data user.
 */
class User(private val user: Long): DataHolder() {
    override fun toString(): String = "User Data Object: $user"
    override fun equals(other: Any?): Boolean = other is User && other.user == user
    override fun hashCode(): Int = user.hashCode()

    /**
     * Updates something
     */
    fun set(key: String, value: Any) {
        API.updateObject(API.Type.USER, user, Pair(key, value)).subscribe()
        data[key] = value
    }
}