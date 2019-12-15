package dev.shog.buta.commands.commands

import dev.shog.buta.commands.obj.ICommand.Companion.COMMANDS
import dev.shog.buta.EN_US
import dev.shog.buta.commands.api.GuildFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.formatText
import dev.shog.buta.util.sendMessage
import dev.shog.buta.util.update
import discord4j.core.`object`.util.Image
import discord4j.core.`object`.util.Permission
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

/**
 * Help
 */
val HELP = Command("help", Categories.INFO) { e, args, lang ->
    if (args.size >= 1) {
        val command = args[0]

        return@Command Flux.fromIterable(COMMANDS)
                .filter { en -> command.startsWith(en.data.commandName.toLowerCase(), true) }
                .filterWhen { en -> en.permable.check(e) }
                .collectList()
                .flatMap { list ->
                    if (list.isNotEmpty()) {
                        list[0].invokeHelp(e)
                    } else e.sendMessage(lang.getString("inv-cmd"))
                }
                .then()
    }

    val helpCommand = Categories.values().toFlux()
            .flatMap { cat ->
                COMMANDS.toFlux()
                        .filter { cmd -> cmd.category == cat }
                        .filterWhen { cmd -> cmd.permable.hasPermission(e.message.author.get()) }
                        .map { cmd -> formatText(lang.getString("command"), cmd.data.commandName) }
                        .collectList()
                        .filter { list -> list.isNotEmpty() }
                        .map { list ->
                            formatText(
                                    lang.getString("cat-pair"),
                                    cat.name.capitalize(),
                                    list
                                            .stream()
                                            .collect(Collectors.joining())
                            )
                        }
                        .map { msg -> msg.substring(0, msg.length - 2) }
            }
            .collectList()
            .map { list -> list.stream().collect(Collectors.joining("\n\n")) }

    return@Command e.message.channel
            .zipWith(helpCommand)
            .flatMap { zip ->
                val ch = zip.t1

                ch.createEmbed { embed ->
                    embed.update(e.message.author.get())
                    embed.setTitle("Help")

                    embed.setDescription(lang.getString("desc") + "\n\n" + zip.t2)
                }
            }
            .then()
}.build().add()

/**
 * Ping
 */
val PING = Command("ping", Categories.INFO) { e, _, lang ->
    e.sendMessage(formatText(lang.getString(lang.keySet().random()), e.client.responseTime))
            .then()
}.build().add()

/**
 * About Buta
 */
val ABOUT = Command("about", Categories.INFO) { e, _, lang ->
    e.sendMessage(lang.getString("default"))
            .then()
}.build().add()

/** The guild date formatter. */
private val FORMATTER = SimpleDateFormat("MM/dd/yyyy")

/**
 * About Guild
 */
val GUILD = Command("guild", Categories.INFO, isPmAvailable = false) { e, args, lang ->
    return@Command when {
        args.size == 0 ->
            e.message.channel
                    .flatMap { ch ->
                        e.message.guild
                                .flatMap { g ->
                                    ch.createEmbed { embed ->
                                        embed.update(e.member.get())

                                        embed.addField(lang.getString("field-name"), g.name, false)
                                        embed.addField(lang.getString("field-userCount"), g.memberCount.asInt.toString(), false)
                                        embed.addField(lang.getString("field-date"), FORMATTER.format(Date.from(g.id.timestamp)), false)

                                        embed.setImage(g.getIconUrl(Image.Format.JPEG).orElse(""))
                                    }
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
                                    ch.createEmbed { embed ->
                                        embed.update(e.member.get())

                                        embed.setDescription(formatText(lang.getString("global-desc"), list.t1, list.t2))

                                        embed.setImage("https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fi.imgur.com%2FgDe7MRn.png&f=1&nofb=1") // PogU
                                    }
                                }
                    }
                    .then()

        else ->
            e.sendMessage(EN_US.get().getJSONObject("error").getString("invalid_arguments"))
                    .then()
    }
}.build().add()