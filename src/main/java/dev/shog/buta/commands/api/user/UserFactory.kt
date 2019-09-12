package dev.shog.buta.commands.api.user

import dev.shog.buta.commands.api.API
import discord4j.core.`object`.util.Snowflake
import org.json.JSONObject
import java.lang.Exception

/**
 * A [User] factory
 */
object UserFactory {
    /**
     * Gets a [User].
     */
    fun getUser(long: Long): User {
        val obj = API.getObject(API.Type.USER, long) ?: return createUser(long)

        return toUserObject(obj, long)
    }

    /**
     * Gets a [User].
     */
    fun getUser(snowflake: Snowflake): User = getUser(snowflake.asLong())

    /**
     * Creates a [User].
     */
    private fun createUser(long: Long): User {
        val obj = API.createObject(API.Type.USER, long) ?: throw Exception("Couldn't create User object!")

        return toUserObject(obj, long)
    }

    /**
     * Turns a [JSONObject] into a [User]
     */
    private fun toUserObject(jsonObject: JSONObject, long: Long): User {
        val user = User(long)

        user.data.apply {
            jsonObject.toMap().forEach { pair ->
                put(pair.key, pair.value)
            }
        }

        return user
    }
}