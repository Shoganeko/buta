package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.ar
import discord4j.core.event.domain.message.MessageCreateEvent
import kong.unirest.Unirest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class DogGalleryCommand : Command(CommandConfig(
        "doggallery",
        "Get a dog picture.",
        Category.FUN,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return Unirest.get("https://api.thedogapi.com/v1/images/search?size=full")
                .asJsonAsync()
                .toMono()
                .map { js -> js.body.array }
                .map { obj -> obj.getJSONObject(0).getString("url") }
                .flatMap { url ->
                    e.message.channel
                            .flatMap { ch ->
                                ch.createEmbed { spec ->
                                    container.getEmbed("embed")
                                            .applyEmbed(spec, e.message.author.get(), hashMapOf("image" to url.ar()))
                                }
                            }
                }
                .then()
    }
}