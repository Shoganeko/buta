package dev.shog.buta

import dev.shog.buta.commands.CommandFactory
import dev.shog.buta.commands.api.guild.GuildFactory
import dev.shog.buta.commands.commands.*
import dev.shog.buta.commands.obj.Command.Companion.COMMANDS
import dev.shog.buta.events.GuildJoinEvent
import dev.shog.buta.events.GuildLeaveEvent
import dev.shog.buta.events.MessageEvent
import dev.shog.buta.events.PresenceHandler
import dev.shog.buta.util.getType
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * The main LOGGER
 */
val LOGGER = LoggerFactory.getLogger("Chad Instance")!!

/**
 * The main Discord Client.
 */
var CLIENT: DiscordClient? = null

fun main() = runBlocking<Unit> {
    val key = FileHandler.get("token")

    if (key == null) {
        LOGGER.error("Please fillout the configuration file!")
        exitProcess(-1)
    }

    COMMANDS.apply {
        add(CommandFactory.build(PING))
        add(CommandFactory.build(HELP))
        add(CommandFactory.build(ABOUT))
        add(CommandFactory.build(PREFIX))
        add(CommandFactory.build(GUILD_INFO))
        add(CommandFactory.build(USER_INFO))
    }

    CLIENT = DiscordClientBuilder(key as String).build().apply {
        eventDispatcher.on(GuildCreateEvent::class.java).subscribe(GuildJoinEvent::invoke)
        eventDispatcher.on(GuildDeleteEvent::class.java).subscribe(GuildLeaveEvent::invoke)
        eventDispatcher.on(MessageCreateEvent::class.java).subscribe(MessageEvent::invoke)
        eventDispatcher.on(ReadyEvent::class.java).subscribe(PresenceHandler::invoke)
    }

    CLIENT?.login()?.block()
}