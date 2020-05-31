package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage
import kong.unirest.Unirest
import reactor.kotlin.core.publisher.toMono

val RANDOM_WORD_COMMAND = Command(CommandConfig("randomword")) {
    Unirest.get("https://random-word-api.herokuapp.com/word")
            .asJsonAsync()
            .toMono()
            .map { js -> js.body.array }
            .map { ar -> ar.getString(0) }
            .flatMap { word -> sendMessage("word", word) }
            .then()
}