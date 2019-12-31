package dev.shog.buta.util

import dev.shog.buta.handle.obj.getField
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import org.json.JSONObject
import reactor.core.publisher.Mono
import java.awt.Color
import java.time.Instant
import kotlin.random.Random

/**
 * Updates [EmbedCreateSpec] with default values.
 */
fun EmbedCreateSpec.updateDefault(color: Color = Color(Random.nextInt(), Random.nextInt(), Random.nextInt())): EmbedCreateSpec {
    setColor(color)
    setTimestamp(Instant.now())

    return this
}

/**
 * Updates [EmbedCreateSpec] with proper footer, and avatar url, and applies [updateDefault].
 */
fun EmbedCreateSpec.update(user: User, color: Color = Color(96, 185, 233)): EmbedCreateSpec {
    setFooter("Requested by ${user.username}", user.avatarUrl)

    return updateDefault(color)
}

/**
 * Send a simple text message in the channel where [MessageCreateEvent] was created.
 */
fun MessageCreateEvent.sendMessage(msg: String): Mono<Message> =
        message.channel
                .flatMap { ch -> ch.createMessage(msg) }

data class FieldReplacement(val title: ArrayList<String>?, val desc: ArrayList<String>?)

/**
 * Remember, if this doesn't work: Double check the YML. If a image URL is wrong, you may have intended URL. idiot
 *
 * Get a [EmbedCreateSpec] from a [JSONObject].
 *
 * This shit is so confusing, so that's why there's so many comments.
 *
 * [replaceable] is replacement for all non-fields. This includes things such as the title.
 * Formatted like: hashMapOf("title" to arrayListOf("replaceWith"))
 *
 * [fields] is replacement for all fields.
 * Formatted like: hashMapOf("field-name" to (arrayListOf("replaceFieldTitle") to arrayListOf("replaceFieldDescription"))
 */
fun JSONObject.applyEmbed(
        spec: EmbedCreateSpec,
        user: User,
        replaceable: HashMap<String, ArrayList<String>> = hashMapOf(),
        fields: HashMap<String, FieldReplacement> = hashMapOf()
) {
    // Different actions.
    hashMapOf<String, EmbedCreateSpec.(String) -> Unit>(
            "title" to { str -> setTitle(str) },
            "desc" to { str -> setDescription(str) },
            "url" to { str -> setUrl(str) },
            "thumb" to { str -> setThumbnail(str) },
            "image" to { str -> setImage(str) }
    ).forEach { (t, u) ->
        if (has(t)) { // If the type ("title" etc) is included, do it.
            val replace = replaceable[t] // Get the included arguments

            val str = if (replace != null)
                getString(t).formArray(replace) // Form the array with the included arguments if included
            else getString(t) // If there's no arguments

            u.invoke(spec, str) // Invoke the setX() function
        }
    }

    if (has("field") && get("field") is JSONObject) { // If there's fields.
        val obj = getJSONObject("field")

        for (fieldKey in obj.keys()) { // For each field
            val rawField = obj.get(fieldKey) // The raw field

            if (rawField is JSONObject) { // If the field is an object
                val fieldObj = rawField.getField() // Get the field object (inline, title, desc)

                if (fields.containsKey(fieldKey)) { // If there's replacements for field.
                    val field = fields[fieldKey]!!

                    val title = field.title ?: arrayListOf() // Replacement for the field title
                    val desc = field.desc ?: arrayListOf() // Replacement for the field description

                    spec.addField(
                            fieldObj.title.formArray(title), // Form the title
                            fieldObj.desc.formArray(desc), // Form the desc
                            fieldObj.inline // gua gua gua
                    )
                } else spec.addField(fieldObj.title, fieldObj.desc, fieldObj.inline) // If there's no replacement
            }
        }
    }

    spec.update(user) // sets colors n stuff
}