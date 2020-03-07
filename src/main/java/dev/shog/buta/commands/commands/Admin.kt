package dev.shog.buta.commands.commands

import dev.shog.buta.commands.api.factory.GuildFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.form
import dev.shog.buta.util.sendMessage
import dev.shog.lib.transport.Duo
import dev.shog.lib.transport.duo
import dev.shog.lib.util.toEnabledDisabled
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.util.stream.Collectors

/**
 * Initialize administrator commands
 */
fun initAdmin() = Unit

/**
 * Prefix
 */
val SET_PREFIX = Command("prefix", Categories.ADMINISTRATOR, permable = PermissionFactory.hasPermission(arrayListOf(Permission.ADMINISTRATOR))) { e, args, lang ->
    if (args.size == 1) {
        val newPrefix = args[0]

        if (newPrefix.length > 3 || newPrefix.isEmpty())
            return@Command e.sendMessage(lang.getString("wrong-length").form(newPrefix.length)).then()

        e.message.guild
                .map { g -> g.id.asLong() }
                .flatMap { id -> GuildFactory.getObject(id) }
                .doOnNext { g -> g.prefix = newPrefix }
                .flatMap { g -> GuildFactory.updateObject(g.id, g) }
                .then(e.sendMessage(lang.getString("set").form(newPrefix)))
                .then()
    } else e.message.guild
            .map { g -> g.id.asLong() }
            .flatMap(GuildFactory::getObject)
            .map { g -> g.prefix }
            .flatMap { p -> e.sendMessage(lang.getString("prefix").form(p)) }
            .then()
}.build().add()

/**
 * Toggle NSFW
 */
val NSFW_TOGGLE = Command("nsfw", Categories.ADMINISTRATOR, isPmAvailable = false, permable = PermissionFactory.hasPermission(arrayListOf(Permission.ADMINISTRATOR))) { e, _, lang ->
    e.message.channel
            .ofType(TextChannel::class.java)
            .flatMap { ch ->
                ch.createMessage(lang.getString("default").form(ch.isNsfw.not().toEnabledDisabled()))
                        .then(ch.edit { che -> che.setNsfw(!ch.isNsfw) })
            }
            .then()
}.build().add()

/**
 * Set swear filter
 */
val SWEAR_FILTER = Command("swearfilter", Categories.ADMINISTRATOR, isPmAvailable = false, permable = PermissionFactory.hasPermission(arrayListOf(Permission.ADMINISTRATOR))) { e, args, lang ->
    when {
        args.isEmpty() -> {
            GuildFactory.getObject(e.guildId.get().asLong())
                    .map { obj -> obj.swearFilter }
                    .flatMap { sf -> e.sendMessage(lang.getString("default").form(sf.first?.toEnabledDisabled(), sf.second)) }
                    .then()
        }

        args.getOrNull(0).equals("message", true) && args.size > 1 -> {
            args.removeAt(0)
            val message = args.stream().collect(Collectors.joining(" ")).trim()

            GuildFactory.getObject(e.guildId.get().asLong())
                    .doOnNext { obj -> obj.swearFilter = Duo(obj.swearFilter.first, message) }
                    .flatMap { obj -> GuildFactory.updateObject(e.guildId.get().asLong(), obj) }
                    .flatMap { e.sendMessage(lang.getString("message").form(message)) }
                    .then()
        }

        args.getOrNull(0).equals("toggle", true) -> {
            var setTo = false
            GuildFactory.getObject(e.guildId.get().asLong())
                    .doOnNext { obj -> setTo = (obj.swearFilter.first ?: false).not() }
                    .doOnNext { obj -> obj.swearFilter = Duo(setTo, obj.swearFilter.second) }
                    .flatMap { obj ->
                        e.sendMessage(lang.getString("toggle").form(setTo))
                                .flatMap { GuildFactory.updateObject(e.guildId.get().asLong(), obj) }
                    }
                    .then()
        }

        else -> e.sendMessage("error.invalid_arguments").then()
    }
}.build().add()


/**
 * Purge messages
 */
val PURGE = Command("purge", Categories.ADMINISTRATOR, isPmAvailable = false, permable = PermissionFactory.hasPermission(arrayListOf(Permission.ADMINISTRATOR))) { e, args, lang ->
    val amount = if (args.size == 1) {
        val pre = args[0].toLongOrNull() ?: -1

        if (pre > 500L || 1L > pre) 100 else pre
    } else 100

    e.message.channel
            .ofType(TextChannel::class.java)
            .flatMapMany { ch ->
                ch.bulkDelete(ch.getMessagesBefore(e.message.id)
                        .take(amount)
                        .map { msg -> msg.id }
                )
            }
            .collectList()
            .flatMap { e.sendMessage(lang.getString("default").form(amount)) }
            .delayElement(Duration.ofSeconds(5))
            .flatMap { msg -> e.message.delete().then(msg.delete()) }
            .then()
}.build().add()

/**
 * Get a role name
 */
internal fun getRole(long: Long?, guild: Mono<Guild>): Mono<String> {
    if (long == null || long == -1L)
        return "none".toMono()

    val role = guild
            .flatMap { g -> g.getRoleById(Snowflake.of(long)) }

    return role.hasElement()
            .flatMap { has ->
                if (has)
                    role.map(Role::getName)
                else "none".toMono()
            }
}

val JOIN_ROLE = Command("joinrole", Categories.ADMINISTRATOR, isPmAvailable = false, permable = PermissionFactory.hasPermission(arrayListOf(Permission.ADMINISTRATOR))) { e, args, lang ->
    if (args.isEmpty()) {
        val guild = GuildFactory.getObject(e.guildId.get().asLong())

        return@Command guild
                .map { g -> g.joinRole }
                .flatMap { role -> Mono.zip(getRole(role.second, e.guild), role.toMono()) }
                .flatMap { zip ->
                    val en = zip.t2.first?.toEnabledDisabled() ?: "disabled"

                    if (zip.t2.second == null)
                        e.sendMessage(lang.getString("un-set").form(en, zip.t1))
                    else e.sendMessage(lang.getString("on-set").form(en, zip.t1))
                }
                .then()
    }

    return@Command when (args.first().toLowerCase()) {
        "role" -> {
            args.removeAt(0)

            val built = args.stream().collect(Collectors.joining(" "))

            e.guild
                    .flatMapMany { guild -> guild.roles }
                    .filter { role -> role.name.equals(built, true) }
                    .map { role -> role.id.asLong() }
                    .defaultIfEmpty(-1L)
                    .flatMap { roleId ->
                        if (roleId == -1L) {
                            e.sendMessage(lang.getString("invalid-role"))
                        } else e.sendMessage(lang.getString("set").form(built.toLowerCase()))
                                .flatMap {
                                    GuildFactory.getObject(e.guildId.get().asLong())
                                            .map { guild -> guild.apply { joinRole = joinRole.first!! duo roleId } }
                                            .flatMap { guild -> GuildFactory.updateObject(guild.id, guild) }
                                }
                    }
                    .then()
        }

        "set" -> {
            if (args.size != 2)
                return@Command e.sendMessage(lang.getString("invalid-args")).then()

            val boolean = args[1].toBoolean()

            e.sendMessage(lang.getString("toggle").form(boolean.toEnabledDisabled()))
                    .flatMap { GuildFactory.getObject(e.guildId.get().asLong()) }
                    .map { guild -> guild.apply { joinRole = boolean duo (joinRole.second ?: -1) } }
                    .flatMap { guild -> GuildFactory.updateObject(guild.id, guild) }
                    .then()
        }

        else -> e.sendMessage(lang.getString("invalid-args")).then()
    }
}.build().add()