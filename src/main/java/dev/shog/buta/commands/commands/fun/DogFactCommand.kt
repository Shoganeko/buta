package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage
import kong.unirest.Unirest

val DOG_FACT_COMMAND = Command(CommandConfig(
        name = "dogfact",
        category = Category.FUN,
        description = "Get pictures of dogs!",
        help = hashMapOf("dogfact" to "Get a random picture of a dog.")
)) {
    val fact = Unirest.get("https://dog-api.kinduff.com/api/facts")
            .asJson()
            .body
            .`object`
            .getJSONArray("facts")
            .first()

    sendMessage(fact as String)
}