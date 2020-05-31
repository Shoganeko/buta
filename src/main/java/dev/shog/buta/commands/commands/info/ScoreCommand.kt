package dev.shog.buta.commands.commands.info

import dev.shog.buta.api.factory.UserFactory
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.sendGlobalMessage
import dev.shog.buta.util.sendMessage
import dev.shog.buta.util.update
import discord4j.core.event.domain.message.MessageCreateEvent
import org.json.JSONArray
import org.json.JSONObject
import reactor.core.publisher.Mono

val DEFAULT_SCORE: JSONArray by lazy {
    val ar = JSONArray()

    ar.put(JSONObject().put("label", "ScoreOne").put("score", 0))
    ar.put(JSONObject().put("label", "ScoreTwo").put("score", 0))
    ar.put(JSONObject().put("label", "ScoreThree").put("score", 0))

    ar
}

val SCORE_COMMAND = Command(CommandConfig("score")) {
    if (!event.message.author.isPresent) // if a webhook tries?
        return@Command Mono.empty<Unit>()

    val author = event.message.author.get()
    val user = UserFactory.getOrCreate(author.id.asLong())
    val json = JSONArray(user.scoreData)

    return@Command when {
        args.isEmpty() -> {
            event.message.channel.flatMap { ch ->
                ch.createEmbed { embed ->
                    embed.update(event.message.author.get())
                    embed.setTitle("Your current score board:")

                    for (i in 0 until json.length()) {
                        val obj = json.getJSONObject(i)

                        val score = obj.getInt("score")
                        val label = obj.getString("label")

                        val symbol = if (score >= 0) "+" else ""

                        embed.addField(label, symbol + score, true)
                    }
                }
            }
        }

        args.size == 3 && args[0].equals("label", true) -> {
            val score = args[1].toIntOrNull()
            val newLabel = args[2]

            if (newLabel.length > 32)
                return@Command sendMessage("too-long")

            for (i in 0 until json.length()) {
                if (json.getJSONObject(i).getString("label").equals(newLabel, true))
                    return@Command event.sendMessage("already-exists")
            }

            if (score != null && 3 >= score) {
                val obj = json.getJSONObject(score - 1)

                if (obj.has("score") && obj.has("label")) {
                    obj.put("label", newLabel)

                    json.put(score - 1, obj)
                    user.scoreData = json.toString()

                    return@Command event.sendMessage("update-label", score, newLabel)
                }
            }

            sendMessage("invalid-number")
        }

        args.size == 2 -> {
            val label = args[0]

            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)

                if (obj.getString("label").equals(label, true)) {
                    val add = parseAddition(obj.getInt("score"), args[1])
                            ?: return@Command sendMessage("invalid-modify")

                    obj.put("score", add)

                    val symbol = if (add >= 0) "+" else ""

                    json.put(i, obj)
                    user.scoreData = json.toString()

                    return@Command sendMessage("update-score", obj.getString("label"), "${symbol}${add}")
                }
            }

            sendGlobalMessage("error.invalid-arguments")
        }

        else -> sendGlobalMessage("error.invalid-arguments")
    }
}

/**
 * Parse [modify] into an integer.
 */
private fun parseAddition(current: Int, modify: String): Int? {
    val modAmount = modify.toIntOrNull()

    if (modAmount == null || current + modAmount > Int.MAX_VALUE)
        return null

    return current + modAmount
}