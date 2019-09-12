package dev.shog.buta.commands.api

import com.mashape.unirest.http.Unirest
import dev.shog.buta.LOGGER
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * The Mojura/Buta API
 */
object API {
    /**
     * The base URL for making request
     */
    private const val BASE_URL = "http://localhost:4070/v1/buta"

    /**
     * Another cache. [There's a cache in Mojura as well]
     */
    private val CACHE = ConcurrentHashMap<Long, JSONObject>()

    /**
     * Gets an object.
     */
    fun getObject(type: Type, id: Long): JSONObject? {
        if (CACHE.contains(id))
            return CACHE[id]

        val resp = Unirest.get(BASE_URL)
                .field("type", type.toString().toLowerCase())
                .field("id", id.toString())
                .asJson()

        if (resp.code != 200)
            return null

        LOGGER.debug("GET: " + resp.body.`object`)

        CACHE[id] = JSONObject(resp.body.`object`.get("response").toString())

        return CACHE[id]
    }

    /**
     * Create an object.
     */
    fun createObject(type: Type, id: Long): JSONObject? {
        if (CACHE.contains(id))
            return CACHE[id]

        val resp = Unirest.put(BASE_URL)
                .field("type", type.toString().toLowerCase())
                .field("id", id.toString())
                .asJson()

        if (resp.code != 200)
            return null

        LOGGER.debug("CREATE: " + resp.body.`object`)

        CACHE[id] = JSONObject(resp.body.`object`.get("response").toString())

        return CACHE[id]
    }

    /**
     * Update an object
     */
    fun updateObject(type: Type, id: Long, pair: Pair<String, Any>): JSONObject? {
        val pairType = when (pair.second) {
            is String -> "string"
            is Long -> "long"
            is Int -> "int"
            is Double -> "double"
            else -> return null
        }

        val resp = Unirest.post(BASE_URL)
                .field("type", type.toString().toLowerCase())
                .field("id", id.toString())
                .field("key", pair.first)
                .field("value", pair.second.toString())
                .field("valueType", pairType)
                .asJson()

        if (resp.code != 200)
            return null

        LOGGER.debug("UPDATE: " + resp.body.`object`)

        CACHE[id] = JSONObject(resp.body.`object`.get("response").toString())

        return CACHE[id]
    }

    /**
     * Deletes an object.
     */
    fun deleteObject(type: Type, id: Long): Boolean {
        val resp = Unirest.delete(BASE_URL)
                .field("type", type.toString().toLowerCase())
                .field("id", id.toString())
                .asJson()

        if (resp.code != 200)
            return false

        LOGGER.debug("DELETE: " + resp.body.`object`)

        try {
            CACHE.remove(id)
        } catch (e: Exception) {
        }

        return true
    }

    /**
     * The type of object to manipulate.
     */
    enum class Type {
        GUILD, USER
    }
}