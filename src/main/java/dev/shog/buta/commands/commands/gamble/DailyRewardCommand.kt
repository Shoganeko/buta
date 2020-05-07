package dev.shog.buta.commands.commands.gamble

import dev.shog.buta.commands.api.factory.UserFactory
import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.fancyDate
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class DailyRewardCommand : Command(CommandConfig(
        "dailyreward",
        "Get a daily reward.",
        Category.GAMBLING,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        val user = UserFactory.getOrCreate(e.message.author.get().id.asLong())

        return when {
            System.currentTimeMillis() - user.lastDailyReward >= TIME_UNTIL_DAILY_REWARD ->
                e.sendMessage(container, "successful", DAILY_REWARD_AMOUNT)
                        .doOnNext {
                            user.bal += DAILY_REWARD_AMOUNT
                            user.lastDailyReward += System.currentTimeMillis()
                        }

            else -> e.sendMessage(
                    container,
                    "unsuccessful",
                    (TIME_UNTIL_DAILY_REWARD - (System.currentTimeMillis() - user.lastDailyReward)).fancyDate()
            )
        }
    }
}