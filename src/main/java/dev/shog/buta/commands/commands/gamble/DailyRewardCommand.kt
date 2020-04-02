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
        return UserFactory.getObject(e.message.author.get().id.asLong())
                .flatMap { user ->
                    when {
                        System.currentTimeMillis() - user.lastDailyReward >= TIME_UNTIL_DAILY_REWARD ->
                            e.sendMessage(container, "successful", DAILY_REWARD_AMOUNT)
                                    .doOnNext {
                                        UserFactory.updateObject(user.id, user.apply {
                                            lastDailyReward = System.currentTimeMillis()
                                            bal += DAILY_REWARD_AMOUNT
                                        })
                                    }

                        else -> e.sendMessage(
                                container,
                                "unsuccessful",
                                (TIME_UNTIL_DAILY_REWARD - (System.currentTimeMillis() - user.lastDailyReward)).fancyDate()
                        )
                    }
                }
    }
}