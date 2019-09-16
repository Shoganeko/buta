package dev.shog.buta.commands.commands

import dev.shog.buta.commands.api.guild.GuildFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.permission.PermissionFactory
import discord4j.core.`object`.util.Permission
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/**
 * The prefix command.
 */
val PREFIX = object : Command("Prefix", "Change or view the prefix in the guild.",
        hashMapOf(Pair("prefix", "View the current prefix."), Pair("prefix [new prefix]", "Update the guild's prefix.")),
        false,
        Categories.ADMINISTRATOR,
        PermissionFactory.hasPermissions({false}, {false}),
        arrayListOf()
) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<Void> {
        if (args.size == 1) {
            if (args[0].length > 4) {
                return e.message.channel
                        .flatMap { ch ->
                            ch.createMessage("Please create a prefix that has less than 4 characters!")
                        }.then()
            }

            return e.message.channel.flatMap { ch ->
                GuildFactory.getGuild(e.guildId.get())
                        .doOnNext { g -> g.set("prefix", args[0]) }
                        .flatMap { g -> ch.createMessage("Your prefix is now: `${g.getString("prefix")}`") }
                        .then()
            }.then()
        } else {
            return e.message.channel.flatMap { ch ->
                GuildFactory.getGuild(e.guildId.get())
                        .flatMap { g -> ch.createMessage("Your prefix is: `${g.getString("prefix")}`") }
                        .then()
            }.then()
        }
    }
}