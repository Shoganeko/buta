package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.ar
import discord4j.core.event.domain.message.MessageCreateEvent
import kong.unirest.Unirest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

val CAT_GALLERY_COMMAND = Command(CommandConfig("catgallery")) {
    Unirest.get("https://api.thecatapi.com/v1/images/search?size=full")
            .asJsonAsync()
            .toMono()
            .map { js -> js.body.array }
            .map { obj -> obj.getJSONObject(0).getString("url") }
            .flatMap { url ->
                event.message.channel
                        .flatMap { ch ->
                            ch.createEmbed { spec ->
                                container.getEmbed("embed")
                                        .applyEmbed(spec, event.message.author.get(), hashMapOf("image" to url.ar()))
                            }
                        }
            }
            .then()
}