package dev.shog.buta.commands.commands.info

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.*
import dev.shog.lib.util.defaultFormat
import discord4j.rest.util.Image

val GUILD_COMMAND = Command(CommandConfig("guild")) {
    return@Command when {
        args.size == 0 ->
            event.message.channel
                    .zipWith(event.message.guild)
                    .flatMap { zip ->
                        val ch = zip.t1
                        val g = zip.t2

                        ch.createEmbed { spec ->
                            container.getEmbed("local-guild").applyEmbed(
                                    spec,
                                    event.message.author.get(),
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
            event.message.channel
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
                                                event.message.author.get(),
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
            sendGlobalMessage("error.invalid-arguments")
    }
}