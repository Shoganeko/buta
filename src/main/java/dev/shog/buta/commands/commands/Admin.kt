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