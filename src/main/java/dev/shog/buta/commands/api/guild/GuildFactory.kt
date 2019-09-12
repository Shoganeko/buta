package dev.shog.buta.commands.api.guild

import dev.shog.buta.commands.api.API
import discord4j.core.`object`.util.Snowflake
import org.json.JSONObject
import java.lang.Exception

/**
 * A [Guild] factory
 */
object GuildFactory {
    /**
     * Gets a [Guild].
     */
    fun getGuild(long: Long): Guild {
        val obj = API.getObject(API.Type.GUILD, long) ?: return createGuild(long)

        return toGuildObject(obj, long)
    }

    /**
     * Gets a [Guild].
     */
    fun getGuild(snowflake: Snowflake): Guild = getGuild(snowflake.asLong())

    /**
     * Creates a [Guild].
     */
    private fun createGuild(long: Long): Guild {
        val obj = API.createObject(API.Type.GUILD, long) ?: throw Exception("Couldn't create Guild object!")

        return toGuildObject(obj, long)
    }

    /**
     * Turns a [JSONObject] into a [Guild]
     */
    private fun toGuildObject(jsonObject: JSONObject, long: Long): Guild {
        val guild = Guild(long)

        guild.data.apply {
            jsonObject.toMap().forEach { pair ->
                put(pair.key, pair.value)
            }
        }

        return guild
    }
}