package dev.shog.buta.commands.commands.info

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.FieldReplacement
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.ar
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.defaultFormat
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Image
import reactor.core.publisher.Mono

class GuildCommand : Command(CommandConfig(
        "guild",
        "View the guild's information",
        Category.INFO,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return when {
            args.size == 0 ->
                e.message.channel
                        .zipWith(e.message.guild)
                        .flatMap { zip ->
                            val ch = zip.t1
                            val g = zip.t2

                            ch.createEmbed { spec ->
                                container.getEmbed("local-guild").applyEmbed(
                                        spec,
                                        e.message.author.get(),
                                        hashMapOf(
                                                "image" to g.getIconUrl(Image.Format.JPEG).orElse("").ar(),
                                                "title" to g.name.ar()
                                        ),
                                        hashMapOf(
                                                "user-count" to FieldReplacement(null, g.memberCount.toString().ar()),
                                                "date" to FieldReplacement(null, g.id.timestamp.defaultFormat().ar())
                                        )
                                )
                            }
                        }
                        .then()

            args[0].equals("global", true) ->
                e.message.channel
                        .flatMap { ch ->
                            ch.client.guilds
                                    .collectList()
                                    .map { list -> list.size }
                                    .zipWith(
                                            ch.client.users
                                                    .collectList()
                                                    .map { list -> list.size }
                                    )
                                    .flatMap { list ->
                                        ch.createEmbed { spec ->
                                            container.getEmbed("local-guild").applyEmbed(
                                                    spec,
                                                    e.message.author.get(),
                                                    hashMapOf(
                                                            "image" to "https://cdn.frankerfacez.com/emoticon/256055/4".ar(),
                                                            "desc" to arrayListOf(list.t1.toString(), list.t2.toString())
                                                    ),
                                                    hashMapOf()
                                            )
                                        }
                                    }
                        }
                        .then()

            else ->
                e.sendMessage("error.invalid-arguments").then()
        }
    }
}