package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import kong.unirest.Unirest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

val CAT_FACT_COMMAND = Command(CommandConfig("catfact")) {
    Unirest.get("https://catfact.ninja/fact")
            .asJsonAsync()
            .toMono()
            .map { js -> js.body.`object` }
            .map { obj -> obj.getString("fact") }
            .flatMap { fact -> sendMessage("fact", fact) }
}