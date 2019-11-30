package dev.shog.buta.handle

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.json.JSONObject
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

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
    private fun toLangInstance(jsonNode: JsonNode, lang: String): LangInstance {
        val li = LangInstance(lang)
        li.phr = JSONObject(jsonNode.toString())
        return li
    }

    /**e
     * A language instance.
     *
     * @param lang The language string. Ex: en_US
     */
    class LangInstance internal constructor(private val lang: String) {
        /**
         * The phrases.
         */
        internal lateinit var phr: JSONObject

        /**
         * Get an instance of [phr].
         */
        fun get() = phr
    }
}