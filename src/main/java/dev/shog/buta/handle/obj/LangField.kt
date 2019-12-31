package dev.shog.buta.handle.obj

import org.json.JSONObject

data class LangField(val title: String, val desc: String, val inline: Boolean)

fun JSONObject.getField(): LangField {
    if (!has("title") || !has("desc") || !has("inline"))
        throw Exception("Missing part of field. ${get("title")} / ${get("desc")} / ${get("inline")}")

    return LangField(getString("title"), getString("desc"), getString("inline")!!.toBoolean())
}