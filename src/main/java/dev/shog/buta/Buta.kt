package dev.shog.buta

//import dev.shog.buta.api.MOJOR_SERVER
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.gitlab.kordlib.cache.map.MapLikeCollection
import com.gitlab.kordlib.cache.map.internal.MapEntryCache
import com.gitlab.kordlib.cache.map.lruLinkedHashMap
import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.channel.GuildMessageChannel
import com.gitlab.kordlib.core.event.gateway.ReadyEvent
import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.guild.GuildDeleteEvent
import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.event.message.MessageDeleteEvent
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.core.on
import com.gitlab.kordlib.gateway.MessageDelete
import dev.shog.buta.commands.CommandHandler
import dev.shog.buta.commands.commands.`fun`.*
import dev.shog.buta.commands.commands.admin.PURGE_COMMAND
import dev.shog.buta.commands.commands.gamble.BALANCE_COMMAND
import dev.shog.buta.commands.commands.gamble.DAILY_REWARD_COMMAND
import dev.shog.buta.commands.commands.info.*
import dev.shog.buta.events.BotMention
import dev.shog.buta.events.GuildJoinEvent
import dev.shog.buta.events.GuildLeaveEvent
import dev.shog.buta.events.MessageEvent
import dev.shog.buta.handle.ButaConfig
import dev.shog.buta.handle.StatisticsManager
import dev.shog.lib.app.Application
import dev.shog.lib.app.cfg.ConfigHandler
import dev.shog.lib.app.cfg.ConfigType
import dev.shog.lib.discord.DiscordWebhook
import dev.shog.lib.discord.WebhookUser
import dev.shog.lib.util.ArgsHandler
import dev.shog.lib.util.defaultFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime


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
) { _, _, cfg ->
    DiscordWebhook(
        cfg.asObject<ButaConfig>().webhook!!,
        WebhookUser("Buta", "https://shog.dev/favicon.png")
    )
}

/**
 * The main Discord Client.
 */
var CLIENT: Kord? = null

/**
 * If Buta is running in production mode.
 */
var PRODUCTION: Boolean = false

val DELETED_MESSAGES = hashMapOf<Snowflake, Message?>()

@ExperimentalCoroutinesApi
@ExperimentalTime
fun main(arg: Array<String>): Unit = runBlocking {
    // mute mongodb
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger = loggerContext.getLogger("org.mongodb.driver")
    rootLogger.level = Level.OFF

//    MOJOR_SERVER.start(false)

    val key = APP.getConfigObject<ButaConfig>().token

    if (key == null) {
        LOGGER.error("Please fill out the configuration file!")
        exitProcess(-1)
    }

    val args = ArgsHandler()

    args.multiHook("--prod", {
        LOGGER.info("Starting Buta in Non-Production mode")

        PRODUCTION = false
    }, {
        LOGGER.info("Starting Buta in Production mode")

        PRODUCTION = true
    })

    args.initWith(arg)

    initCommands()

    Runtime.getRuntime().addShutdownHook(Thread { StatisticsManager.save() })

    Timer().schedule(timerTask {
        StatisticsManager.save()
    }, 0, 1000 * 60 * 60) // Hourly

    val kord = Kord(key) {
        cache {
            users { cache, description ->
                MapEntryCache(cache, description, MapLikeCollection.concurrentHashMap())
            }

            messages { cache, description ->
                MapEntryCache(cache, description, MapLikeCollection.lruLinkedHashMap(maxSize = 1000))
            }

            members { cache, description ->
                MapEntryCache(cache, description, MapLikeCollection.none())
            }
        }
    }

    CLIENT = kord

    kord.on<MessageCreateEvent> {
        MessageEvent.invoke(this)
        BotMention.invoke(this)
    }

    kord.on<GuildCreateEvent> {
        GuildJoinEvent.invoke(this)
    }

    kord.on<GuildDeleteEvent> {
        GuildLeaveEvent.invoke(this)
    }

    kord.on<MemberJoinEvent> {
        dev.shog.buta.events.MemberJoinEvent.invoke(this)
    }

    kord.on<MessageDeleteEvent> {
        val id = guildId

        if (id != null) {
            println(message)
            DELETED_MESSAGES[id] = message
        }
    }

    kord.on<ReactionAddEvent> {
        if (wildWaiting.containsKey(userId) && properColors.contains(emoji)) {
            val time = wildWaiting[userId]?.time ?: 0

            if (System.currentTimeMillis() - time < TimeUnit.SECONDS.toMillis(10))
                completedWildCard(this)
        }
    }

    kord.on<ReadyEvent> {
        println("Bot is online")
//
//        val guild = kord.getGuild(Snowflake(669815533566296064))
//
//        if (guild != null) {
//            val gmc = guild.getChannel(Snowflake(724189047378935879L)) as GuildMessageChannel
//
//            val file = File("E:\\top-nathans.txt")
//
//            if (!file.exists())
//                file.createNewFile()
//
//            val writer = FileWriter(file)
//
//            gmc.messages
//                    .collect { message ->
//                        delay(250L)
//                        writer.write("\n\n[${message.timestamp.defaultFormat()}] ${message.author?.username}: ${message.content}")
//                    }
//
//            writer.close()
//        }
    }

    kord.login()
}

/**
 * Initialize commands
 */
@ExperimentalCoroutinesApi
@ExperimentalTime
private fun initCommands() {
    CommandHandler.add(
        AVATAR_COMMAND,
        PING_COMMAND,
        SCORE_COMMAND,
        HELP_COMMAND,
        GUILD_COMMAND,
        ABOUT_COMMAND,
        BALANCE_COMMAND,
        DAILY_REWARD_COMMAND,
        UNO_COMMAND,
        WORD_REVERSE_COMMAND,
        DOG_GALLERY_COMMAND,
        DOG_FACT_COMMAND,
        CAT_GALLERY_COMMAND,
        CAT_FACT_COMMAND,
        PURGE_COMMAND,
        STOCK_VIEW_COMMAND,
        SNIPE_COMMAND
    )
}