package dev.shog.buta.api.factory

import dev.shog.buta.api.obj.ButaObject
import dev.shog.buta.api.obj.User
import java.util.concurrent.ConcurrentHashMap

/**
 * Creates and manages [User]s.
 */
abstract class ButaFactory<T : ButaObject> {
    /**
     * A user's ID to their [ButaObject] instance.
     */
    val cache = ConcurrentHashMap<Long, T>()

    /**
     * Update a user by using their [obj] to set [key]. This is using [obj] since it needs to reset the cache.
     */
    abstract fun updateObject(obj: T, key: Pair<String, Any>)

    /**
     * Create a [ButaObject] with the ID [id].
     */
    abstract fun createObject(id: Long): T

    /**
     * Delete a [ButaObject] with the ID [id].
     */
    abstract fun deleteObject(id: Long)

    /**
     * See if a [ButaObject] exists.
     */
    fun objectExists(id: Long): Boolean =
            getObject(id) != null

    /**
     * [getObject] or [createObject] if it doesn't exist.
     */
    fun getOrCreate(id: Long): T =
            getObject(id) ?: createObject(id)

    /**
     * Get object with [id].
     */
    abstract fun getObject(id: Long): T?
}