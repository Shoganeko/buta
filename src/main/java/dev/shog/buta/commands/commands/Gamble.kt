package dev.shog.buta.commands.commands

import dev.shog.buta.commands.api.factory.UserFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.util.form
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.fancyDate

/**
 * Gamble Balance
 */
val GAMBLE_BALANCE = Command("balance", Categories.GAMBLING) { e, _, lang ->
    e.message.userMentions
            .collectList()
            .flatMap { mentions ->
                if (mentions.isNotEmpty()) {
                    val id = mentions[0].id.asLong()

                    UserFactory.getObject(id)
                            .map { user -> user.bal }
                            .flatMap { bal -> e.sendMessage(lang.getString("other").form(mentions[0].username, bal)) }
                } else {
                    val id = e.message.author.get().id.asLong()

                    UserFactory.getObject(id)
                            .map { user -> user.bal }
                            .flatMap { bal -> e.sendMessage(lang.getString("self").form(bal)) }
                }
            }
            .then()
}.build().add()

/**
 * Get a daily reward.
 */
val DAILY_REWARD = Command("dailyreward", Categories.GAMBLING) { e, _, lang ->
    UserFactory.getObject(e.message.author.get().id.asLong())
            .flatMap { user ->
                when {
                    System.currentTimeMillis() - user.lastDailyReward >= TIME_UNTIL_DAILY_REWARD ->
                        e.sendMessage(lang.getString("successful").form(DAILY_REWARD_AMOUNT))
                                .doOnNext {
                                    UserFactory.updateObject(user.id, user.apply {
                                        lastDailyReward = System.currentTimeMillis()
                                        bal += DAILY_REWARD_AMOUNT
                                    })
                                }

                    else -> e.sendMessage(lang.getString("unsuccessful").form(
                            (TIME_UNTIL_DAILY_REWARD - (System.currentTimeMillis() - user.lastDailyReward)).fancyDate()
                    ))
                }
            }
            .then()
}.build().add()

/**
 * The interval between rewards
 */
const val TIME_UNTIL_DAILY_REWARD = 1000 * 60 * 60 * 24

/**
 * The amount the user should be rewarded with
 */
const val DAILY_REWARD_AMOUNT = 2000