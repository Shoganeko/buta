package dev.shog.buta.commands.api.obj

import org.json.JSONArray
import org.json.JSONObject

open class DataHolder: DataObject() {
    /**
     * Gets something from [data].
     */
    fun get(key: String): Any? = data[key]

    /**
     * Gets something and turns it into a string.
     * If it's null, returns "null".
     */
    fun getString(key: String): String? =
            if (get(key).toString() == "null")
                null
            else get(key).toString()

    /**
     * Gets something and turns it into an int.
     */
    fun getInt(key: String): Int? = getString(key)?.toIntOrNull()

    /**
     * Gets something and turns it into an long.
     */
    fun getLong(key: String): Long? = getString(key)?.toLongOrNull()

    /**
     * Gets something and turns it into a json object.
     */
    fun getJSONObject(key: String): JSONObject? = try {
        JSONObject(getString(key))
    } catch (ex: Exception) { null }

    /**
     * Gets something and turns it into a json array.
     */
    fun getJSONArray(key: String): JSONArray? = try {
        JSONArray(getString(key))
    } catch (ex: Exception) { null }

    /**
     * Gets something and turns it into an array list.
     * This goes from a string, json array, then an array list
     */
    fun getArrayList(key: String): ArrayList<Any>? = arrayListOf<Any>().apply {
        (getJSONArray(key) ?: return null).forEach { obj ->
            this.add(obj)
        }
    }
}