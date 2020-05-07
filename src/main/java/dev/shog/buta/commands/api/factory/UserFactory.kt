package dev.shog.buta.commands.api.factory

import dev.shog.buta.commands.api.obj.User
import dev.shog.buta.commands.commands.info.ScoreCommand
import dev.shog.buta.handle.PostgreSql

/**
 * Manage [User]'s.
 */
object UserFactory : ButaFactory<User>() {
    /**
     * Create a [User] with their [id].
     */
    override fun createObject(id: Long): User {
        val user = User(id, 0, -1, 0, ScoreCommand.DEFAULT.toString())

        PostgreSql.createConnection()
                .prepareStatement("INSERT INTO buta.users (id, bal, last_daily_reward, xp, score_data) VALUES (?, ?, ?, ? ,?)")
                .apply {
                    setLong(1, id)
                    setLong(2, user.bal)
                    setLong(3, user.lastDailyReward)
                    setLong(4, user.xp)
                    setString(5, user.scoreData)
                }
                .executeUpdate()

        cache[id] = user

        return user
    }

    /**
     * Delete an object with an [id].
     */
    override fun deleteObject(id: Long) {
        PostgreSql.createConnection()
                .prepareStatement("DELETE FROM buta.users WHERE id = ?")
                .apply { setLong(1, id) }
                .executeUpdate()

        cache.remove(id)
    }

    /**
     * Get a [User] by their [id].
     */
    override fun getObject(id: Long): User? {
        if (cache.containsKey(id))
            return cache[id]!!

        val rs = PostgreSql.createConnection()
                .prepareStatement("SELECT * FROM buta.users WHERE id = ?")
                .apply { setLong(1, id) }
                .executeQuery()

        while (rs.next()) {
            val user = User(
                    rs.getLong("id"),
                    rs.getLong("bal"),
                    rs.getLong("last_daily_reward"),
                    rs.getLong("xp"),
                    rs.getString("score_data")
            )

            cache[rs.getLong("id")] = user

            return user
        }

        return null
    }

    /**
     * Update [obj] using [key].
     */
    override fun updateObject(obj: User, key: Pair<String, Any>) {
        cache[obj.id] = obj

        PostgreSql.createConnection()
                .prepareStatement("UPDATE buta.users SET ${key.first} = ? WHERE id = ?")
                .apply {
                    setObject(1, key.second)

                    setLong(2, obj.id)
                }
                .executeUpdate()
    }
}