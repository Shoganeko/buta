package dev.shog.buta.handle.obj

import org.json.JSONObject

/**
 * A field for an Embed, created from a [JSONObject].
 *
 * @param title The embed title.
 * @param desc The embed description.
 * @param inline If the embed is inline.
 */
data class LangField(val title: String, val desc: String, val inline: Boolean)

/**
 * Get a [LangField] from a [JSONObject]
 */
fun JSONObject.getField(): LangField {
    if (!has("title") || !has("desc") || !has("inline"))
        throw Exception("Missing part of field. ${get("title")} / ${get("desc")} / ${get("inline")}")

    return LangField(getString("title"), getString("desc"), getString("inline")!!.toBoolean())
}