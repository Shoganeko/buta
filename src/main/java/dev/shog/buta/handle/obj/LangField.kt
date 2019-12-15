package dev.shog.buta.handle.obj

import org.json.JSONObject

data class LangField(val title: String, val desc: String)

fun JSONObject.getField(): LangField {
    if (!has("title") || !has("desc"))
        return LangField("Missing Title", "Missing Description")

    return LangField(getString("title"), getString("desc"))
}