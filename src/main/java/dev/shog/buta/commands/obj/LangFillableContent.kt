package dev.shog.buta.commands.obj

import dev.shog.buta.EN_US
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

/**
 * Command data that can be filled from lang.
 */
data class LangFillableContent(
        val commandName: String,
        val commandDesc: String,
        val alias: MutableList<String>,
        val helpCommand: HashMap<String, String>
) {
    companion object {
        /**
         * Get a [LangFillableContent] from [name].
         */
        fun getFromCommandName(name: String): LangFillableContent {
            val lang = EN_US

            if (!lang.has(name) && lang[name] is JSONObject)
                return LangFillableContent("ERROR", "Could not find data in language yml!", mutableListOf(), hashMapOf())

            val cmd = lang.getJSONObject(name)

            val alias = if (cmd.has("alias"))
                getAliases(cmd.getJSONArray("alias"))
            else mutableListOf()


            if (!cmd.has("name") || !cmd.has("desc") || !cmd.has("help"))
                return LangFillableContent("ERROR", "Could not find data in language yml!", mutableListOf(), hashMapOf())

            return LangFillableContent(
                    cmd.getString("name"),
                    cmd.getString("desc"),
                    alias,
                    getCommands(cmd.getJSONArray("help"))
            )
        }

        /**
         * Get aliases from [ar].
         */
        private fun getAliases(ar: JSONArray): MutableList<String> {
            val list = mutableListOf<String>()

            ar.toMutableList().forEach { obj ->
                list.add(obj as? String ?: "--${Random.nextInt()}")
            }

            return list
        }

        /**
         * Get commands from [ar].
         */
        private fun getCommands(ar: JSONArray): HashMap<String, String> {
            val map = hashMapOf<String, String>()

            ar.forEach { obj ->
                if (obj is JSONObject) {
                    val objKeys = obj.keys()

                    if (objKeys.hasNext()) {
                        val key = objKeys.next()

                        map[key] = obj[key] as? String
                                ?: "Error"
                    }
                }
            }

            return map
        }
    }
}