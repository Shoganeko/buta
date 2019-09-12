package dev.shog.buta.commands.commands

import dev.shog.buta.commands.api.guild.GuildFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import discord4j.core.event.domain.message.MessageCreateEvent

/**
 * The prefix command.
 */
val PREFIX = object : Command("Prefix", "Change or view the prefix in the guild.",
        hashMapOf(Pair("prefix", "View the current prefix."), Pair("prefix [new prefix]", "Update the guild's prefix.")),
        false, Categories.ADMINISTRATOR, arrayListOf()
) {
    override suspend fun invoke(e: MessageCreateEvent, args: MutableList<String>) {
        if (args.size == 1) {
            if (args[0].length > 4) {
                e.message.channel
                        .flatMap { ch ->
                            ch.createMessage("Please create a prefix that has less than 4 characters!")
                        }.subscribe()
                return
            }

            e.message.channel.subscribe { ch ->
                val guild = GuildFactory.getGuild(e.guildId.get())

                guild.set("prefix", args[0])

                ch.createMessage("Your prefix is now: `${guild.getString("prefix")}`").subscribe()
            }
        } else {
            e.message.channel
                    .flatMap { ch ->
                        ch.createMessage("Your prefix is: `${GuildFactory.getGuild(e.guildId.get()).getString("prefix")}`")
                    }.subscribe()
        }
    }
}