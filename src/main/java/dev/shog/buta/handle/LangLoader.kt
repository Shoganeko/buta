package dev.shog.buta.handle

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.json.JSONObject

/**
 * Load languages.
 *
 * Hopefully some time in the future I add support for different languages.
 * Used mostly for easily changeable common phrases.
 */
object LangLoader {
    /**
     * The loaded languages from [loadLang]
     */
    private val loadedLanguages = HashMap<String, LangInstance>()

    /**
     * Load the [lang].
     *
     * @throws Exception [lang] cannot be found.
     */
    fun loadLang(lang: String): LangInstance {
        if (loadedLanguages.containsKey(lang))
            return loadedLanguages[lang]!!

        val inpStr = this::class.java.classLoader.getResourceAsStream("$lang.yml")
                ?: throw Exception("Invalid language!")

        loadedLanguages[lang] = toLangInstance(ObjectMapper(YAMLFactory()).readTree(inpStr), lang)

        return loadedLanguages[lang]!!
    }

    /**
     * Get a [lang] instance from [loadedLanguages].
     *
     * @throws Exception [lang] hasn't been loaded yet
     */
    fun getLang(lang: String): LangInstance =
            loadedLanguages[lang] ?: throw Exception("This language hasn't been loaded yet!")

    /**
     * Turn a [jsonNode] and [lang] into a [LangInstance]
     */
    private fun toLangInstance(jsonNode: JsonNode, lang: String): LangInstance =
            LangInstance(lang, jsonNode.toString())

    /**e
     * A language instance.
     *
     * @param lang The language string. Ex: en_US
     * @param langString The actual language.
     */
    data class LangInstance internal constructor(private val lang: String, private val langString: String) : JSONObject(langString) {
        /**
         * Get a string using an error like `errors.niceerror.invalidargs`
         */
        fun getEntry(entry: String): String {
            val split = entry.split(".").toMutableList()
            val last = split.last()

            split.removeAt(split.size - 1)

            var pointer = this as JSONObject
            for (spl in split) {
                pointer = pointer.getJSONObject(spl)
            }

            return pointer.getString(last)
        }
    }

    /**
     * A full message data pack.
     */
    data class FullMessageDataPack(val error: JSONObject, val success: JSONObject, val embeds: JSONObject, val other: JSONObject)
}