package dev.shog.buta

import dev.shog.buta.api.MOJOR_SERVER
import dev.shog.buta.commands.CommandHandler
import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.commands.commands.`fun`.*
import dev.shog.buta.commands.commands.admin.*
import dev.shog.buta.commands.commands.dev.PRESENCE_COMMAND
import dev.shog.buta.commands.commands.dev.STAT_DUMP_COMMAND
import dev.shog.buta.commands.commands.dev.THREAD_VIEW_COMMAND
import dev.shog.buta.commands.commands.gamble.BALANCE_COMMAND
import dev.shog.buta.commands.commands.gamble.DAILY_REWARD_COMMAND
import dev.shog.buta.commands.commands.info.*
import dev.shog.buta.commands.commands.music.*
import dev.shog.buta.events.GuildJoinEvent
import dev.shog.buta.events.GuildLeaveEvent
import dev.shog.buta.events.MessageEvent
import dev.shog.buta.events.PresenceHandler
import dev.shog.buta.handle.ButaConfig
import dev.shog.buta.handle.StatisticsManager
import dev.shog.buta.handle.audio.AudioManager
import dev.shog.lib.app.Application
import dev.shog.lib.app.cfg.ConfigHandler
import dev.shog.lib.app.cfg.ConfigType
import dev.shog.lib.discord.DiscordWebhook
import dev.shog.lib.discord.WebhookUser
import dev.shog.lib.util.ArgsHandler
import dev.shog.lib.util.logDiscord
import discord4j.common.util.Snowflake
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
val APP = Application(
        "Buta",
        "2.1.0",
        ConfigHandler.useConfig(ConfigType.YML, "buta", ButaConfig())
) { _, _, cfg -> DiscordWebhook(cfg.asObject<ButaConfig>().webhook!!, WebhookUser("Buta", "https://shog.dev/favicon.png")) }

/**
 * The main Discord Client.
 */
var CLIENT: GatewayDiscordClient? = null

/**
 * If Buta is running in production mode.
 */
var PRODUCTION: Boolean = false

fun main(arg: Array<String>) {
    MOJOR_SERVER.start(false)

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

    Hooks.onOperatorError { t, _ -> t.logDiscord(APP) }
    Hooks.onErrorDropped { e -> e.logDiscord(APP) }
    Hooks.onNextError { e, _ -> e.logDiscord(APP) }

    args.initWith(arg)

    initCommands()

    Runtime.getRuntime().addShutdownHook(Thread { StatisticsManager.save() })

    Timer().schedule(timerTask {
        StatisticsManager.save()
    }, 0, 1000 * 60 * 60) // Hourly

    DiscordClientBuilder.create(key)
            .build()
            .gateway()
            .setSharding(ShardingStrategy.recommended())
            .setInitialStatus { Presence.online(Activity.watching("you")) }
            .login()
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
                    if (wildWaiting.containsKey(ev.userId) && properColors.contains(ev.emoji)) {
                        val time = wildWaiting[ev.userId]?.time ?: 0

                        if (System.currentTimeMillis() - time < TimeUnit.SECONDS.toMillis(10))
                            completedWildCard(ev).then()
                        else Mono.empty<Unit>().then()
                    } else Mono.empty<Unit>().then()
                }.subscribe()

                // stop voice when disconnected
                gw.on(VoiceStateUpdateEvent::class.java) { ev ->
                    ev.toMono()
                            .filter { event ->
                                event.client.selfId == event.current.userId
                            }
                            .filter { event -> !event.current.channelId.isPresent }
                            .map { event -> AudioManager.getGuildMusicManager(event.current.guildId) }
                            .doOnNext { guild -> guild.stop(false) }
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
                                GuildFactory.getOrCreate(e.guildId.asLong()).toMono()
                                        .map { guild -> guild.joinRole }
                                        .filter { role -> role != -1L }
                                        .filterWhen { role ->
                                            e.client.self
                                                    .flatMap { self -> self.asMember(e.guildId) }
                                                    .zipWith(e.guild.flatMap { guild ->
                                                        guild.getRoleById(Snowflake.of(role))
                                                    })
                                                    .flatMap { zip -> zip.t1.hasHigherRoles(setOf(zip.t2.id)) }
                                        }
                                        .flatMap { role -> e.member.addRole(Snowflake.of(role)) }
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
    CommandHandler.add(LEAVE_COMMAND,
            PAUSE_COMMAND,
            PLAY_COMMAND,
            QUEUE_COMMAND,
            SKIP_COMMAND,
            VOLUME_COMMAND,
            STOCK_VIEW_COMMAND,
            PING_COMMAND,
            SCORE_COMMAND,
            HELP_COMMAND,
            GUILD_COMMAND,
            ABOUT_COMMAND,
            BALANCE_COMMAND,
            DAILY_REWARD_COMMAND,
            UNO_COMMAND,
            REDDIT_COMMAND,
            WORD_REVERSE_COMMAND,
            REDDIT_COMMAND,
            RANDOM_WORD_COMMAND,
            DOG_GALLERY_COMMAND,
            DOG_FACT_COMMAND,
            CAT_GALLERY_COMMAND,
            CAT_FACT_COMMAND,
            PRESENCE_COMMAND,
            STAT_DUMP_COMMAND,
            THREAD_VIEW_COMMAND,
            JOIN_ROLE_COMMAND,
            NSFW_TOGGLE_COMMAND,
            PURGE_COMMAND,
            SET_PREFIX_COMMAND,
            SWEAR_FILTER_COMMAND
    )
}