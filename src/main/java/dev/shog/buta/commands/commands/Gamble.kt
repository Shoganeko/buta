package dev.shog.buta.commands.commands

import dev.shog.buta.commands.api.UserFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.util.form
import dev.shog.buta.util.sendMessage

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
                            .flatMap { bal -> e.sendMessage(lang.getString("other").form(mentions[0].username, bal)) }
                } else {
                    val id = e.message.author.get().id.asLong()

                    UserFactory.get(id)
                            .map { user -> user.bal }
                            .flatMap { bal -> e.sendMessage(lang.getString("self").form(bal)) }
                }
            }
            .then()
}.build().add()