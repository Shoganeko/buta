package dev.shog.buta

import dev.shog.buta.commands.CommandHandler
import dev.shog.buta.commands.api.factory.GuildFactory
import dev.shog.buta.commands.commands.`fun`.*
import dev.shog.buta.commands.commands.admin.*
import dev.shog.buta.commands.commands.dev.PresenceCommand
import dev.shog.buta.commands.commands.dev.StatDumpCommand
import dev.shog.buta.commands.commands.dev.ThreadViewCommand
import dev.shog.buta.commands.commands.gamble.BalanceCommand
import dev.shog.buta.commands.commands.gamble.DailyRewardCommand
import dev.shog.buta.commands.commands.info.*
import dev.shog.buta.commands.commands.music.*
import dev.shog.buta.commands.list.music.LeaveCommand
import dev.shog.buta.events.GuildJoinEvent
import dev.shog.buta.events.GuildLeaveEvent
import dev.shog.buta.events.MessageEvent
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
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import discord4j.core.event.domain.guild.MemberJoinEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.shard.ShardingStrategy
import discord4j.rest.util.Permission
import discord4j.rest.util.Snowflake
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess


/** Developers */
val DEV = arrayOf(274712215024697345L, 173495550467899402L)

/**
 * The main LOGGER
 */
val LOGGER = LoggerFactory.getLogger("Buta Instance")!!

/**
 * App
 */
val APP = AppBuilder("Buta", 2.0F)
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

fun main(arg: Array<String>) {
    val key = APP.getConfigObject<ButaConfig>().token

    if (key == null) {
        LOGGER.error("Please fill out the configuration file!")
        exitProcess(-1)
    }

    val args = ArgsHandler()

    args.multiHook("--prod", {
        LOGGER.info("Starting Buta in Non-Production mode")

        PRODUCTION = false

        Hooks.onOperatorDebug()
    }, {
        LOGGER.info("Starting Buta in Production mode")

        PRODUCTION = true
    })

    args.nHook("--block-init-notif") {
        runBlocking {
            APP.getWebhook().sendMessage("Buta (v${APP.getVersion()}) is now online!")
        }
    }

    Hooks.onOperatorError { t, _ -> t.logDiscord(APP) }
    Hooks.onErrorDropped { e -> e.logDiscord(APP) }
    Hooks.onNextError { e, _ -> e.logDiscord(APP) }

    args.initWith(arg)

    initCommands()

    Runtime.getRuntime().addShutdownHook(Thread {
        StatisticsManager.save()

        runBlocking {
            APP.getWebhook().sendMessage("Buta (v${APP.getVersion()}) is now offline! :(")
        }
    })

    Timer().schedule(timerTask {
        StatisticsManager.save()
    }, 0, 1000 * 60 * 60) // Hourly

    DiscordClientBuilder.create(key)
            .build()
            .gateway()
            .setSharding(ShardingStrategy.recommended())
            .setInitialStatus { Presence.online(Activity.watching("you")) }
            .connect()
            .doOnNext { gw ->
                CLIENT = gw

                // a guild event
                gw.on(GuildCreateEvent::class.java) {
                    GuildJoinEvent.invoke(it).then()
                }.subscribe()

                // a guild leave event
                gw.on(GuildDeleteEvent::class.java) {
                    GuildLeaveEvent.invoke(it).then()
                }.subscribe()

                // a message event
                gw.on(MessageCreateEvent::class.java) {
                    MessageEvent.invoke(it).then()
                }.subscribe()

                // for b!uno
                gw.on(ReactionAddEvent::class.java) { ev ->
                    if (Uno.wildWaiting.containsKey(ev.userId) && Uno.properColors.contains(ev.emoji)) {
                        val time = Uno.wildWaiting[ev.userId]?.time ?: 0

                        if (System.currentTimeMillis() - time < TimeUnit.SECONDS.toMillis(10))
                            Uno.completedWildCard(ev)
                    }

                    Mono.empty<Unit>()
                }.subscribe()

                // stop voice when disconnected
                gw.on(VoiceStateUpdateEvent::class.java) { ev ->
                    ev.toMono()
                            .filterWhen { event ->
                                event.client.selfId
                                        .map { id -> id == event.current.userId }
                            }
                            .filter { event -> !event.current.channelId.isPresent }
                            .map { event -> AudioManager.getGuildMusicManager(event.current.guildId) }
                            .doOnNext { guild -> guild.stop(true) }
                            .then()
                }.subscribe()

                // a member join event
                gw.on(MemberJoinEvent::class.java) { event ->
                    event.toMono().filterWhen { e ->
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
                                                    .zipWith(e.guild.flatMap { guild ->
                                                        guild.getRoleById(Snowflake.of(duo.second!!))
                                                    })
                                                    .flatMap { zip -> zip.t1.hasHigherRoles(setOf(zip.t2.id)) }
                                        }
                                        .flatMap { duo -> e.member.addRole(Snowflake.of(duo.second!!)) }
                            }
                            .then()
                }.subscribe()

                // a ready event
                gw.on(ReadyEvent::class.java, PresenceHandler::invoke).subscribe()
            }
            .block()
}

/**
 * Initialize commands
 */
private fun initCommands() {
    CommandHandler.add(LeaveCommand(),
            PauseCommand(),
            PlayCommand(),
            QueueCommand(),
            SkipCommand(),
            VolumeCommand(),
            StockViewCommand(),
            PingCommand(),
            HelpCommand(),
            GuildCommand(),
            AboutCommand(),
            BalanceCommand(),
            DailyRewardCommand(),
            Uno,
            RedditCommand(),
            WordReverseCommand(),
            RedditCommand(),
            RandomWordCommand(),
            DogGalleryCommand(),
            DogFactCommand(),
            CatGalleryCommand(),
            CatFactCommand(),
            PresenceCommand(),
            StatDumpCommand(),
            ThreadViewCommand(),
            JoinRoleCommand(),
            NsfwToggleCommand(),
            PurgeCommand(),
            SetPrefixCommand(),
            SwearFilterCommand()
    )
}