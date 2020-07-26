package dev.shog.buta.api.factory

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import dev.shog.buta.api.obj.User
import dev.shog.buta.commands.commands.info.DEFAULT_SCORE
import dev.shog.buta.handle.Mongo
import org.bson.Document

/**
 * Manage [User]'s.
 */
object UserFactory : ButaFactory<User>() {
    /**
     * Create a [User] with their [id].
     */
    override fun createObject(id: Long): User {
        val user = User(id, 0, -1, 0, DEFAULT_SCORE.toString())

        Mongo.getClient()
                .getDatabase("buta")
                .getCollection("users")
                .insertOne(Document(mapOf(
                        "id" to id,
                        "bal" to user.bal,
                        "last_daily_reward" to user.lastDailyReward,
                        "xp" to user.xp,
                        "score_data" to user.scoreData
                )))

        cache[id] = user

        return user
    }

    /**
     * Delete an object with an [id].
     */
    override fun deleteObject(id: Long) {
        Mongo.getClient()
                .getDatabase("buta")
                .getCollection("users")
                .deleteOne(Filters.eq("id", id))

        cache.remove(id)
    }

    /**
     * Get a [User] by their [id].
     */
    override fun getObject(id: Long): User? {
        if (cache.containsKey(id))
            return cache[id]!!

        return Mongo.getClient()
                .getDatabase("buta")
                .getCollection("users")
                .find(Filters.eq("id", id))
                .map { doc ->
                    User(
                            doc.getLong("id"),
                            doc.getLong("bal"),
                            doc.getLong("last_daily_reward"),
                            doc.getLong("xp"),
                            doc.getString("score_data")
                    )
                }
                .firstOrNull()
    }

    /**
     * Update [obj] using [key].
     */
    override fun updateObject(obj: User, key: Pair<String, Any>) {
        cache[obj.id] = obj

        Mongo.getClient()
                .getDatabase("buta")
                .getCollection("users")
                .updateOne(Filters.eq("id", obj.id), Updates.set(key.first, key.second))
    }
}