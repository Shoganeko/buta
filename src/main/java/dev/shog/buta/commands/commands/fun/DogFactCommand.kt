package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage
import kong.unirest.Unirest
import reactor.kotlin.core.publisher.toMono

val DOG_FACT_COMMAND = Command(CommandConfig("dogfact")) {
    Unirest.get("https://dog-api.kinduff.com/api/facts")
            .asJsonAsync()
            .toMono()
            .map { js -> js.body.`object` }
            .map { obj -> obj.getJSONArray("facts").first() }
            .flatMap { fact -> sendMessage("fact", fact as String) }
}