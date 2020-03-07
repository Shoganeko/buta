package dev.shog.buta.handle.msg

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import dev.shog.buta.util.form
import org.json.JSONObject

/**
 * Manage messages
 */
object MessageHandler {
    val data: JSONObject

    init {
        val tree = ObjectMapper(YAMLFactory())
                .readTree(this::class.java.classLoader.getResourceAsStream("msg.yml"))

        data = JSONObject(tree.toString())
    }

    /**
     * Get a message.
     */
    fun getMessage(entry: String): String {
        val split = entry.split(".").toMutableList()
        val last = split.last()

        split.removeAt(split.size - 1)

        var pointer = data
        for (spl in split) {
            pointer = pointer.getJSONObject(spl)
        }

        return pointer.getString(last)
    }

    /**
     * Get a message and form.
     */
    fun getMessage(entry: String, vararg form: Any?): String =
            getMessage(entry).form(*form)

    /**
     * A full message data pack.
     */
    data class FullMessageDataPack(
            val error: JSONObject,
            val success: JSONObject,
            val embeds: JSONObject,
            val other: JSONObject
    )
}