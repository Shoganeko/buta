package dev.shog.buta

import dev.shog.DiscordWebhookHandler
import dev.shog.buta.commands.commands.*
import dev.shog.buta.commands.obj.ICommand
import dev.shog.buta.events.GuildJoinEvent
import dev.shog.buta.events.GuildLeaveEvent
import dev.shog.buta.events.PresenceHandler
import dev.shog.buta.handle.LangLoader
import dev.shog.buta.handle.audio.AudioManager
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.shard.ShardingClientBuilder
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import reactor.core.publisher.Hooks
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * The main LOGGER
 */
val LOGGER = LoggerFactory.getLogger("Buta Instance")!!

/**
 * EN US language
 */
val EN_US = LangLoader.loadLang("en_US")

/**
 * The main Discord Client.
 */
var CLIENT: DiscordClient? = null

/**
 * If Buta is running in production mode.
 */
var PRODUCTION: Boolean = false

fun main(args: Array<String>) = runBlocking<Unit> {
    val key = FileHandler.get("token")

    DiscordWebhookHandler.init()

    if (key == null) {
        LOGGER.error("Please fill out the configuration file!")
        exitProcess(-1)
    }

    when {
        args.contains("--prod") -> {
            LOGGER.info("Starting Buta in Production mode")

            PRODUCTION = true
        }

        !args.contains("--prod") -> {
            LOGGER.info("Starting Buta in Non-Production mode")

            PRODUCTION = false

            Hooks.onOperatorDebug() // this adds extra debug onto reactor stuff, super cool
        }
    }

    initCommands()

    CLIENT = ShardingClientBuilder(key as String)
            .build()
            .map(DiscordClientBuilder::build)
            .blockFirst()


    CLIENT?.apply {
        eventDispatcher.on(GuildCreateEvent::class.java)
                .flatMap { GuildJoinEvent.invoke(it) }
                .subscribe()

        eventDispatcher.on(GuildDeleteEvent::class.java)
                .flatMap { GuildLeaveEvent.invoke(it) }
                .subscribe()

        eventDispatcher.on(MessageCreateEvent::class.java)
                .flatMap { dev.shog.buta.events.MessageEvent.invoke(it) }
                .subscribe()

        eventDispatcher.on(ReactionAddEvent::class.java)
                .filter { event -> Uno.wildWaiting.containsKey(event.userId) }
                .filter { event -> Uno.properColors.contains(event.emoji) }
                .filter { event ->
                    val time = Uno.wildWaiting[event.userId]?.time ?: 0

                    // Make sure the request isn't 10 seconds old TODO purge if so
                    System.currentTimeMillis() - time < TimeUnit.SECONDS.toMillis(10)
                }
                .flatMap { ev -> Uno.completedWildCard(ev) }
                .subscribe()

        eventDispatcher.on(VoiceStateUpdateEvent::class.java)
                .doOnNext { event ->
                    if (event.old.isPresent)
                        AudioManager.getGuildMusicManager(event.current.guildId).stop()
                }
                .subscribe()

        eventDispatcher.on(ReadyEvent::class.java)
                .flatMap { PresenceHandler.invoke(it) }
                .subscribe()
    }

    CLIENT?.login()?.block()
}

/**
 * Each command adds itself to the command list. This just activates that.
 */
private fun initCommands() {
    PING
    ABOUT
    GUILD
    HELP
    MUSIC_PLAY
    SET_PREFIX
    NSFW_TOGGLE
    GAMBLE_BALANCE
    PURGE
    ICommand.COMMANDS.add(Uno)
}