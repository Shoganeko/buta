package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import kong.unirest.Unirest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class RandomWordCommand : Command(CommandConfig(
        "randomword",
        "Get a random word.",
        Category.FUN,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return Unirest.get("https://random-word-api.herokuapp.com/word?key=LN5TCZP1")
                .asJsonAsync()
                .toMono()
                .map { js -> js.body.array }
                .map { ar -> ar.getString(0) }
                .flatMap { word -> e.sendMessage(container, "word", word) }
                .then()
    }
}