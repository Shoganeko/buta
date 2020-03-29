package dev.shog.buta

import dev.shog.buta.commands.api.factory.GuildFactory
import dev.shog.buta.commands.commands.*
import dev.shog.buta.commands.obj.ICommand
import dev.shog.buta.events.GuildJoinEvent
import dev.shog.buta.events.GuildLeaveEvent
import dev.shog.buta.events.PresenceHandler
import dev.shog.buta.handle.ButaConfig
import dev.shog.buta.handle.StatisticsManager
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.lib.app.AppBuilder
import dev.shog.lib.app.cfg.ConfigHandler
import dev.shog.lib.hook.DiscordWebhook
import dev.shog.lib.hook.WebhookUser
import dev.shog.lib.util.ArgsHandler
import dev.shog.lib.util.logDiscord
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import discord4j.core.event.domain.guild.MemberJoinEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.rest.util.Permission
import discord4j.rest.util.Snowflake
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import reactor.core.publisher.Hooks
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

/** Developers */
val DEV = arrayOf(274712215024697345L)

/**
 * The main LOGGER
 */
val LOGGER = LoggerFactory.getLogger("Buta Instance")!!

/**
 * App
 */
val APP = AppBuilder("Buta", 1.2F)
        .usingConfig(ConfigHandler.createConfig(ConfigHandler.ConfigType.YML, "buta", ButaConfig()))
        .configureConfig { cfg ->
            val obj = cfg.asObject<ButaConfig>()
            useCache = true
            logger = LoggerFactory.getLogger("Buta")
            webhook = DiscordWebhook(obj.webhook!!, WebhookUser("Buta", ""))
        }
        .build()

/**
 * The main Discord Client.
 */
var CLIENT: GatewayDiscordClient? = null

/**
 * If Buta is running in production mode.
 */
var PRODUCTION: Boolean = false

fun main(arg: Array<String>) = runBlocking<Unit> {
    val key = APP.getConfigObject<ButaConfig>().token

    if (key == null) {
        LOGGER.error("Please fill out the configuration file!")
        exitProcess(-1)
    }

    val args = ArgsHandler()

    args.multiHook("--prod", {
        LOGGER.info("Starting Buta in Non-Production mode")

        PRODUCTION = false

        Hooks.onOperatorDebug() // this adds extra debug onto reactor stuff, super cool
    }, {
        LOGGER.info("Starting Buta in Production mode")

        PRODUCTION = true
    })

    args.nHook("--block-init-notif") {
        runBlocking { APP.getWebhook().sendMessage("Buta (v${APP.getVersion()}) is now online!") }
    }

    Hooks.onErrorDropped { it.logDiscord(APP) }

    args.initWith(arg)

    initCommands()

    Runtime.getRuntime().addShutdownHook(Thread(StatisticsManager::save))

    Timer().schedule(timerTask {
        StatisticsManager.save()
    }, 0, 1000 * 60 * 60) // Hourly

    DiscordClient
            .create(key)
            .login()
            .doOnNext { cli -> CLIENT = cli }
            .doOnNext { cli ->
                cli.on(GuildCreateEvent::class.java)
                        .flatMap { GuildJoinEvent.invoke(it) }
                        .subscribe()

                cli.on(GuildDeleteEvent::class.java)
                        .flatMap { GuildLeaveEvent.invoke(it) }
                        .subscribe()

                cli.on(MessageCreateEvent::class.java)
                        .flatMap { dev.shog.buta.events.MessageEvent.invoke(it) }
                        .subscribe()

                cli.on(ReactionAddEvent::class.java)
                        .filter { event -> Uno.wildWaiting.containsKey(event.userId) && Uno.properColors.contains(event.emoji) }
                        .filter { event ->
                            val time = Uno.wildWaiting[event.userId]?.time ?: 0

                            // Make sure the request isn't 10 seconds old TODO purge if so
                            System.currentTimeMillis() - time < TimeUnit.SECONDS.toMillis(10)
                        }
                        .flatMap { ev -> Uno.completedWildCard(ev) }
                        .subscribe()

                cli.on(VoiceStateUpdateEvent::class.java)
                        .filterWhen { event ->
                            event.client.selfId
                                    .map { id -> id == event.current.userId }
                        }
                        .filter { event -> !event.current.channelId.isPresent }
                        .map { event -> AudioManager.getGuildMusicManager(event.current.guildId) }
                        .doOnNext { guild -> guild.stop(true) }
                        .subscribe()

                cli.on(MemberJoinEvent::class.java)
                        .filterWhen { e ->
                            e.client.self
                                    .flatMap { self -> self.asMember(e.guildId) }
                                    .flatMap { member -> member.basePermissions }
                                    .map { perms -> perms.contains(Permission.ADMINISTRATOR) }
                        }
                        .flatMap { e ->
                            GuildFactory.getObject(e.guildId.asLong())
                                    .map { guild -> guild.joinRole }
                                    .filter { duo -> duo.first == true && duo.second != null && duo.second != -1L }
                                    .filterWhen { duo ->
                                        e.client.self
                                                .flatMap { self -> self.asMember(e.guildId) }
                                                .zipWith(e.guild.flatMap { guild -> guild.getRoleById(Snowflake.of(duo.second!!)) })
                                                .flatMap { zip -> zip.t1.hasHigherRoles(setOf(zip.t2.id)) }
                                    }
                                    .flatMap { duo -> e.member.addRole(Snowflake.of(duo.second!!)) }
                        }
                        .subscribe()

                cli.on(ReadyEvent::class.java)
                        .flatMap { PresenceHandler.invoke(it) }
                        .subscribe()
            }
            .flatMap { cli -> cli.onDisconnect() }
            .block()
}

/**
 * Initialize commands
 */
private fun initCommands() {
    initInfo()
    initAudio()
    initFun()
    initAdmin()
    initDev()
    initGambling()
    ICommand.COMMANDS.add(Uno)
}