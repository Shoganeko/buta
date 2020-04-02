package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.buta.handle.audio.GuildMusicManager
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import kong.unirest.Unirest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class CatFactCommand : Command(CommandConfig(
        "catfact",
        "Get a cat fact.",
        Category.FUN,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return Unirest.get("https://catfact.ninja/fact")
                .asJsonAsync()
                .toMono()
                .map { js -> js.body.`object` }
                .map { obj -> obj.getString("fact") }
                .flatMap { fact -> e.sendMessage(container, "fact", fact) }
    }
}