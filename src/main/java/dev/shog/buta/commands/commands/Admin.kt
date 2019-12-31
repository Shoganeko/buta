package dev.shog.buta.commands.commands

import dev.shog.buta.commands.api.GuildFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.enabledDisabled
import dev.shog.buta.util.form
import dev.shog.buta.util.sendMessage
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.`object`.util.Permission
import java.time.Duration

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
                .flatMap { id -> GuildFactory.get(id) }
                .doOnNext { g -> g.prefix = newPrefix }
                .flatMap { g -> GuildFactory.update(g.id, g) }
                .then(e.sendMessage(lang.getString("set").form(newPrefix)))
                .then()
    } else e.message.guild
            .map { g -> g.id.asLong() }
            .flatMap(GuildFactory::get)
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
                ch.createMessage(lang.getString("default").form(ch.isNsfw.not().enabledDisabled()))
                        .then(ch.edit { che -> che.setNsfw(!ch.isNsfw) })
            }
            .then()
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