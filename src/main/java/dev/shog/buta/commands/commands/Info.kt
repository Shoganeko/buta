package dev.shog.buta.commands.commands

import dev.shog.buta.EN_US
import dev.shog.buta.commands.obj.InfoCommand
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.formatText
import dev.shog.buta.util.sendMessage
import dev.shog.buta.util.update
import discord4j.core.`object`.util.Image
import java.text.SimpleDateFormat
import java.util.*

/**
 * Ping
 */
val PING = InfoCommand("ping", true, PermissionFactory.hasPermission()) { it, resp ->
    it.first
            .sendMessage(formatText(resp.getString(resp.keySet().random()), it.first.client.responseTime))
            .then()
}.build().add()

/**
 * About Buta
 */
val ABOUT = InfoCommand("about", true, PermissionFactory.hasPermission()) { it, resp ->
    it.first
            .sendMessage(resp.getString("default"))
            .then()
}.build().add()

/** The guild date formatter. */
private val FORMATTER = SimpleDateFormat("MM/dd/yyyy")

/**
 * About Guild
 */
val GUILD = InfoCommand("guild", false, PermissionFactory.hasPermission()) { it, resp ->
    return@InfoCommand when {
        it.second.size == 0 ->
            it.first.message.channel
                    .flatMap { ch ->
                        it.first.message.guild
                                .flatMap { g ->
                                    ch.createEmbed { embed ->
                                        embed.update(it.first.member.get())

                                        embed.addField(resp.getString("field-name"), g.name, false)
                                        embed.addField(resp.getString("field-userCount"), g.memberCount.asInt.toString(), false)
                                        embed.addField(resp.getString("field-date"), FORMATTER.format(Date.from(g.id.timestamp)), false)

                                        embed.setImage(g.getIconUrl(Image.Format.JPEG).orElse(""))
                                    }
                                }
                    }
                    .then()

        it.second[0].equals("global", true) ->
            it.first.message.channel
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
                                    ch.createEmbed { embed ->
                                        embed.update(it.first.member.get())

                                        embed.setDescription(formatText(resp.getString("global-desc"), list.t1, list.t2))

                                        embed.setImage("https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fi.imgur.com%2FgDe7MRn.png&f=1&nofb=1") // PogU
                                    }
                                }
                    }
                    .then()

        else ->
            it.first.sendMessage(EN_US.get().getJSONObject("error").getString("invalid_arguments")).then()
    }
}.build().add()