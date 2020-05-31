package dev.shog.buta.commands.commands.gamble

import dev.shog.buta.api.factory.UserFactory
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.fancyDate

val DAILY_REWARD_COMMAND = Command(CommandConfig("dailyreward")) {
    val user = UserFactory.getOrCreate(event.message.author.get().id.asLong())

    return@Command when {
        System.currentTimeMillis() - user.lastDailyReward >= TIME_UNTIL_DAILY_REWARD ->
            sendMessage("successful", DAILY_REWARD_AMOUNT)
                    .doOnNext {
                        user.bal += DAILY_REWARD_AMOUNT
                        user.lastDailyReward += System.currentTimeMillis()
                    }

        else -> sendMessage(
                "unsuccessful",
                (TIME_UNTIL_DAILY_REWARD - (System.currentTimeMillis() - user.lastDailyReward)).fancyDate()
        )
    }
}