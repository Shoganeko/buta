package dev.shog.buta.commands.commands

import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.InfoCommand
import dev.shog.buta.util.update
import discord4j.core.event.domain.message.MessageCreateEvent

/**
 * About Buta
 */
val PING = InfoCommand("Ping", "Pong!", hashMapOf(Pair("ping", "Get the ping of the bot.")), true, arrayListOf()) {
    it.first.message.channel
            .flatMap { channel ->
                channel.createEmbed  {
                    embed -> embed.update(it.first.message.author.get())
                    embed.setDescription("Pong! ${it.first.client.responseTime}ms")
                }
            }.subscribe()
}

/**
 * About Buta
 */
val ABOUT = InfoCommand("About", "About Buta!", hashMapOf(Pair("about", "About Chad!")), true, arrayListOf()) {
    it.first.message.channel
            .flatMap { channel ->
                channel.createEmbed  { embed ->
                    embed.update(it.first.message.author.get())

                    embed.setTitle("ℹ About Buta")
                    embed.setDescription("Buta, formerly Chad, is a feature filled Discord bot with moderation commands to gambling commands.")
                    embed.setUrl("https://github.com/shoganeko/buta")
                }
            }.subscribe()
}