package dev.shog.buta.api.factory

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import dev.shog.buta.api.obj.Guild
import dev.shog.buta.api.obj.User
import dev.shog.buta.handle.Mongo
import org.bson.Document

/**
 * Manage [User]'s.
 */
object GuildFactory : ButaFactory<Guild>() {
    /**
     * Create a [User] with their [id].
     */
    override fun createObject(id: Long): Guild {
        val guild = Guild(id, "b!", "", "", -1L, "{user}, you can't swear!", false, listOf())

        Mongo.getClient()
                .getDatabase("buta")
                .getCollection("guilds")
                .insertOne(Document(mapOf(
                        "id" to id,
                        "prefix" to guild.prefix,
                        "join_message" to guild.joinMessage,
                        "leave_message" to guild.leaveMessage,
                        "join_role" to guild.joinRole,
                        "swear_filter_msg" to guild.swearFilterMsg,
                        "disabled_categories" to guild.disabledCategories,
                        "swear_filter_on" to guild.swearFilterOn
                )))

        cache[id] = guild

        return guild
    }

    /**
     * Delete an object with an [id].
     */
    override fun deleteObject(id: Long) {
        Mongo.getClient()
                .getDatabase("buta")
                .getCollection("guilds")
                .deleteOne(Filters.eq("id", id))

        cache.remove(id)
    }

    /**
     * Get a [User] by their [id].
     */
    override fun getObject(id: Long): Guild? {
        if (cache.containsKey(id))
            return cache[id]!!

        val guild = Mongo.getClient()
                .getDatabase("buta")
                .getCollection("guilds")
                .find(Filters.eq("id", id))
                .map { doc ->
                    Guild(
                            doc.getLong("id"),
                            doc.getString("prefix"),
                            doc.getString("join_message"),
                            doc.getString("leave_message"),
                            doc.getLong("join_role"),
                            doc.getString("swear_filter_msg"),
                            doc.getBoolean("swear_filter_on"),
                            doc["disabled_categories"] as List<String>
                    )
                }
                .firstOrNull()

        if (guild != null) {
            cache[guild.id] = guild
            return guild
        }

        return null
    }

    /**
     * Update [obj] using [key].
     */
    override fun updateObject(obj: Guild, key: Pair<String, Any>) {
        cache[obj.id] = obj

        Mongo.getClient()
                .getDatabase("buta")
                .getCollection("guilds")
                .updateOne(Filters.eq("id", obj.id), Updates.set(key.first, key.second))
    }
}