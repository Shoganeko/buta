package dev.shog.buta.commands.commands

import dev.shog.buta.commands.api.GuildFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.enabledDisabled
import dev.shog.buta.util.formatText
import dev.shog.buta.util.sendMessage
import discord4j.core.`object`.entity.GuildMessageChannel
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.`object`.util.Permission
import discord4j.rest.json.request.BulkDeleteRequest
import java.time.Duration

/**
 * Prefix
 */
val SET_PREFIX = Command("prefix", Categories.ADMINISTRATOR, permable = PermissionFactory.hasPermission(arrayListOf(Permission.ADMINISTRATOR))) { e, args, lang ->
    if (args.size == 1) {
        val newPrefix = args[0]

        if (newPrefix.length > 3 || newPrefix.isEmpty())
            return@Command e.sendMessage(formatText(lang.getString("wrong-length"), newPrefix.length)).then()

        e.message.guild
                .map { g -> g.id.asLong() }
                .flatMap { id -> GuildFactory.get(id) }
                .doOnNext { g -> g.prefix = newPrefix }
                .flatMap { g -> GuildFactory.update(g.id, g) }
                .then(e.sendMessage(formatText(lang.getString("set"), newPrefix)))
                .then()
    } else e.message.guild
            .map { g -> g.id.asLong() }
            .flatMap { id -> GuildFactory.get(id) }
            .map { g -> g.prefix }
            .flatMap { prefix -> e.sendMessage(formatText(lang.getString("prefix"), prefix)) }
            .then()
}.build().add()

/**
 * Toggle NSFW
 */
val NSFW_TOGGLE = Command("nsfw", Categories.ADMINISTRATOR, isPmAvailable = false, permable = PermissionFactory.hasPermission(arrayListOf(Permission.ADMINISTRATOR))) { e, _, lang ->
    e.message.channel
            .ofType(TextChannel::class.java)
            .flatMap { ch ->
                ch.createMessage(formatText(lang.getString("default"), (!ch.isNsfw).enabledDisabled()))
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

        if (pre > 500L || 1L > pre)
            100
        else pre
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
            .flatMap { e.sendMessage(formatText(lang.getString("default"), amount)) }
            .delayElement(Duration.ofSeconds(5))
            .flatMap { msg -> e.message.delete().then(msg.delete()) }
            .then()
}.build().add()