package dev.shog.buta.commands.commands

import dev.shog.buta.commands.api.GuildFactory
import dev.shog.buta.commands.api.UserFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.formatText
import dev.shog.buta.util.sendMessage
import discord4j.core.`object`.util.Permission

/**
 * Gamble Balance
 */
val GAMBLE_BALANCE = Command("balance", Categories.GAMBLING) { e, _, lang ->
    e.message.userMentions
            .collectList()
            .flatMap { mentions ->
                if (mentions.isNotEmpty()) {
                    val id = mentions[0].id.asLong()

                    UserFactory.get(id)
                            .map { user -> user.bal }
                            .flatMap { bal -> e.sendMessage(formatText(lang.getString("other"), mentions[0].username, bal)) }
                } else {
                    val id = e.message.author.get().id.asLong()

                    UserFactory.get(id)
                            .map { user -> user.bal }
                            .flatMap { bal -> e.sendMessage(formatText(lang.getString("self"), bal)) }
                }
            }
            .then()
}.build().add()