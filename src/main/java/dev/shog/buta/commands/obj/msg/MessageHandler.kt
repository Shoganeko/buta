package dev.shog.buta.commands.obj.msg

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.json.JSONArray
import org.json.JSONObject

object MessageHandler {
    val data: JSONObject by lazy {
        val reader = MessageHandler::class.java.getResourceAsStream("/msg.yml")

        JSONObject(ObjectMapper(YAMLFactory()).readTree(reader.readBytes()).toString())
    }

    /**
     * A message container for a message.
     *
     * @param key The command name.
     */
    class MessageContainer constructor(private val key: String) {
        /**
         * A message with [link] or [args].
         */
        fun getMessage(link: String, vararg args: Any?): String =
                MessageHandler.getMessage("$key.response.$link", *args)

        /**
         * A message with [link] or [args].
         */
        fun getEmbed(link: String): JSONObject =
                MessageHandler.getObject("$key.embeds.$link")

        /**
         * The name from the YML file.
         */
        val name = MessageHandler.getMessage("$key.name")

        /**
         * The description from the YML file.
         */
        val desc = MessageHandler.getMessage("$key.desc")

        /**
         * The aliases from the YML file.
         */
        val aliases = getArray("$key.alias")

        /**
         * The help from the YML file.
         */
        val help = getArray("$key.help")
    }

    /**
     * Get a container with [name].
     */
    fun getContainer(name: String): MessageContainer =
            MessageContainer(name)

    /**
     * Get [message].
     */
    fun getMessage(message: String): String {
        val split = message.split(".").toMutableList()
        val msg = split.last()
        split.removeAt(split.size - 1)

        var pointer = data
        for (spl in split) {
            pointer = pointer.getJSONObject(spl)
        }

        return pointer.getString(msg)
    }

    /**
     * Get a [link] and format with [args].
     */
    fun getMessage(link: String, vararg args: Any?): String =
            formatText(getMessage(link), args.toList().map { it.toString() })

    /**
     * Get a JSONObject by it's link.
     */
    fun getObject(obj: String): JSONObject {
        val split = obj.split(".").toMutableList()

        var pointer = data
        for (spl in split) {
            pointer = pointer.getJSONObject(spl)
        }

        return pointer
    }

    /**
     * Get a JSONArray by it's link.
     */
    fun getArray(ar: String): JSONArray {
        val split = ar.split(".").toMutableList()
        val msg = split.last()
        split.removeAt(split.size - 1)

        var pointer = data
        for (spl in split) {
            pointer = pointer.getJSONObject(spl)
        }

        return pointer.getJSONArray(msg)
    }

    /**
     * Get a [message] and format with [args].
     */
    fun getMessage(message: String, args: Collection<Any?>) =
            formatText(getMessage(message), args.toList().map { it.toString() })

    /**
     * Format [str] with [args]
     */
    fun formatText(str: String, args: Collection<*>): String {
        var newString = str

        args.forEachIndexed { i, arg ->
            if (newString.contains("{$i}"))
                newString = newString.replace("{$i}", arg.toString())
        }

        return newString
    }
}