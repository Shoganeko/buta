package dev.shog.buta.commands.api.factory

import dev.shog.buta.commands.api.obj.Guild
import dev.shog.buta.commands.api.obj.User
import dev.shog.buta.handle.PostgreSql
import dev.shog.lib.util.eitherOr

/**
 * Manage [User]'s.
 */
object GuildFactory : ButaFactory<Guild>() {
    /**
     * Create a [User] with their [id].
     */
    override fun createObject(id: Long): Guild {
        val guild = Guild(id, "b!", "", "", -1L, "{user}, you can't swear!", false, "[]")

        PostgreSql.createConnection()
                .prepareStatement("INSERT INTO buta.guilds (id, prefix, join_message, leave_message, join_role, swear_filter_msg, disabled_categories, swear_filter_on) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                .apply {
                    setLong(1, id)
                    setString(2, guild.prefix)
                    setString(3, guild.joinMessage)
                    setString(4, guild.leaveMessage)
                    setLong(5, guild.joinRole)
                    setString(6, guild.swearFilterMsg)
                    setString(7, guild.disabledCategories)
                    setInt(8, guild.swearFilterOn.eitherOr(1, 0))
                }
                .executeUpdate()

        cache[id] = guild

        return guild
    }

    /**
     * Delete an object with an [id].
     */
    override fun deleteObject(id: Long) {
        PostgreSql.createConnection()
                .prepareStatement("DELETE FROM buta.guilds WHERE id = ?")
                .apply { setLong(1, id) }
                .executeUpdate()

        cache.remove(id)
    }

    /**
     * Get a [User] by their [id].
     */
    override fun getObject(id: Long): Guild? {
        if (cache.containsKey(id))
            return cache[id]!!

        val rs = PostgreSql.createConnection()
                .prepareStatement("SELECT * FROM buta.guilds WHERE id = ?")
                .apply { setLong(1, id) }
                .executeQuery()

        while (rs.next()) {
            val guild = Guild(
                    rs.getLong("id"),
                    rs.getString("prefix"),
                    rs.getString("join_message"),
                    rs.getString("leave_message"),
                    rs.getLong("join_role"),
                    rs.getString("swear_filter_msg"),
                    rs.getInt("swear_filter_on") == 1,
                    rs.getString("disabled_categories")
            )

            cache[rs.getLong("id")] = guild

            return guild
        }

        return null
    }

    /**
     * Update [obj] using [key].
     */
    override fun updateObject(obj: Guild, key: Pair<String, Any>) {
        cache[obj.id] = obj

        PostgreSql.createConnection()
                .prepareStatement("UPDATE buta.guilds SET ${key.first} = ? WHERE id = ?")
                .apply {
                    setObject(1, key.second)
                    setLong(2, obj.id)
                }
                .executeUpdate()
    }
}