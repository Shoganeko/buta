package dev.shog.buta.commands.api.guild

import dev.shog.buta.commands.api.API
import dev.shog.buta.commands.api.obj.DataHolder

/**
 * A data guild.
 */
class Guild(private val guild: Long): DataHolder() {
    override fun toString(): String = "Guild Data Object: $guild"
    override fun equals(other: Any?): Boolean = other is Guild && other.guild == guild
    override fun hashCode(): Int = guild.hashCode()

    /**
     * Updates something
     */
    fun set(key: String, value: Any) {
        API.updateObject(API.Type.GUILD, guild, Pair(key, value))

        data[key] = value
    }
}