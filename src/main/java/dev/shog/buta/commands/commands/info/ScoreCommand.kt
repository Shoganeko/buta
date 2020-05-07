package dev.shog.buta.commands.commands.info

import dev.shog.buta.commands.api.factory.UserFactory
import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.buta.util.update
import discord4j.core.event.domain.message.MessageCreateEvent
import org.json.JSONArray
import org.json.JSONObject
import reactor.core.publisher.Mono

class ScoreCommand : Command(CommandConfig(
        "score",
        "The score command.",
        Category.INFO,
        PermissionFactory.hasPermission()
)) {
    companion object {
        val DEFAULT: JSONArray by lazy {
            val ar = JSONArray()

            ar.put(JSONObject().put("label", "ScoreOne").put("score", 0))
            ar.put(JSONObject().put("label", "ScoreTwo").put("score", 0))
            ar.put(JSONObject().put("label", "ScoreThree").put("score", 0))

            ar
        }
    }

    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        if (!e.message.author.isPresent) // if a webhook tries?
            return Mono.empty<Unit>()

        val author = e.message.author.get()
        val user = UserFactory.getOrCreate(author.id.asLong())
        val json = JSONArray(user.scoreData)

        return when {
            args.isEmpty() -> {
                e.message.channel.flatMap { ch ->
                    ch.createEmbed { embed ->
                        embed.update(e.message.author.get())
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
                    return e.sendMessage(container, "too-long")

                for (i in 0 until json.length()) {
                    if (json.getJSONObject(i).getString("label").equals(newLabel, true))
                        return e.sendMessage(container, "already-exists")
                }

                if (score != null && 3 >= score) {
                    val obj = json.getJSONObject(score - 1)

                    if (obj.has("score") && obj.has("label")) {
                        obj.put("label", newLabel)

                        json.put(score - 1, obj)
                        user.scoreData = json.toString()

                        return e.sendMessage(container, "update-label", score, newLabel)
                    }
                }


                e.sendMessage(container, "invalid-number")
            }

            args.size == 2 -> {
                val label = args[0]

                for (i in 0 until json.length()) {
                    val obj = json.getJSONObject(i)

                    if (obj.getString("label").equals(label, true)) {
                        val add = parseAddition(obj.getInt("score"), args[1])
                                ?: return e.sendMessage(container, "invalid-modify")

                        obj.put("score", add)

                        val symbol = if (add >= 0) "+" else ""

                        json.put(i, obj)
                        user.scoreData = json.toString()

                        return e.sendMessage(container, "update-score", obj.getString("label"), "${symbol}${add}")
                    }
                }

                e.sendMessage("error.invalid-arguments").then()
            }


            else -> e.sendMessage("error.invalid-arguments").then()
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
}