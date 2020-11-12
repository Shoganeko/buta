package dev.shog.buta.commands.commands.info

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import dev.shog.buta.api.factory.UserFactory
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.addFooter
import dev.shog.buta.util.sendMessage
import org.json.JSONArray
import org.json.JSONObject

val DEFAULT_SCORE: JSONArray by lazy {
    val ar = JSONArray()

    ar.put(JSONObject().put("label", "ScoreOne").put("score", 0))
    ar.put(JSONObject().put("label", "ScoreTwo").put("score", 0))
    ar.put(JSONObject().put("label", "ScoreThree").put("score", 0))

    ar
}

val SCORE_COMMAND = Command(CommandConfig(
        name = "score",
        description = "Keep the score of something.",
        help = hashMapOf(
                "score {label} +/-x" to "Adjust a score.",
                "score label 1/2/3 {label}" to "Change a score's label."
        ),
        category = Category.INFO
)) {
    val author = event.message.author ?: return@Command

    val user = UserFactory.getOrCreate(author.id.longValue)
    val json = JSONArray(user.scoreData)

    when {
        args.isEmpty() -> {
            event.message.channel.createEmbed {
                addFooter(event)

                for (i in 0 until json.length()) {
                    val obj = json.getJSONObject(i)

                    val score = obj.getInt("score")
                    val label = obj.getString("label")

                    val symbol = if (score >= 0) "+" else ""

                    field(label, true) { symbol + score }
                }
            }
        }

        args.size == 3 && args[0].equals("label", true) -> {
            val score = args[1].toIntOrNull()
            val newLabel = args[2]

            if (newLabel.length > 32) {
                sendMessage("That label is too long! (Must be under 32)")
                return@Command
            }

            for (i in 0 until json.length()) {
                if (json.getJSONObject(i).getString("label").equals(newLabel, true)) {
                    sendMessage("That label already exists!")
                    return@Command
                }
            }

            if (score != null && 3 >= score) {
                val obj = json.getJSONObject(score - 1)

                if (obj.has("score") && obj.has("label")) {
                    obj.put("label", newLabel)

                    json.put(score - 1, obj)
                    user.scoreData = json.toString()

                    sendMessage("Score number `${score}` label's now `${newLabel}`!")
                    return@Command
                }
            }

            sendMessage("The score should be 1, 2, or 3.")
        }

        args.size == 2 -> {
            val label = args[0]

            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)

                if (obj.getString("label").equals(label, true)) {
                    val add = parseAddition(obj.getInt("score"), args[1])

                    if (add == null) {
                        sendMessage("The modifier should be in the format of `-x` or `+x`.")
                        return@Command
                    }

                    obj.put("score", add)

                    val symbol = if (add >= 0) "+" else ""

                    json.put(i, obj)
                    user.scoreData = json.toString()

                    sendMessage("`${obj.getString("label")}` is now `${symbol}${add}`")
                    return@Command
                }
            }

            sendMessage("Invalid arguments")
        }

        else -> sendMessage("Invalid arguments")
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