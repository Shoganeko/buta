package dev.shog.buta.commands.commands.gamble

import dev.shog.buta.api.factory.UserFactory
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.fancyDate

val DAILY_REWARD_COMMAND = Command(CommandConfig(
        name = "dailyreward",
        category = Category.GAMBLING,
        description = "Get a daily reward of ${DAILY_REWARD_AMOUNT}.",
        help = hashMapOf("dailyreward" to "Receive a daily reward of ${DAILY_REWARD_AMOUNT} in Buta currency.")
)) {
    val user = UserFactory.getOrCreate(event.message.author?.id?.longValue!!)

    when {
        System.currentTimeMillis() - user.lastDailyReward >= TIME_UNTIL_DAILY_REWARD -> {
            sendMessage("You have received your daily reward of `${DAILY_REWARD_AMOUNT}`.")

            user.bal += DAILY_REWARD_AMOUNT
            user.lastDailyReward = System.currentTimeMillis()
        }

        else -> {
            val time = (TIME_UNTIL_DAILY_REWARD - (System.currentTimeMillis() - user.lastDailyReward)).fancyDate()
            sendMessage("You need to wait `${time}` until you can get your daily reward!")
        }
    }
}