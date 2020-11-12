package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage
import kong.unirest.Unirest

val CAT_FACT_COMMAND = Command(CommandConfig("catfact", Category.FUN)) {
    val fact = Unirest.get("https://catfact.ninja/fact")
            .asJson()
            .body
            .`object`
            .getString("fact")

    sendMessage(fact)
}