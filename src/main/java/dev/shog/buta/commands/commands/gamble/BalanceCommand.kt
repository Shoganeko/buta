package dev.shog.buta.commands.commands.gamble

import dev.shog.buta.commands.api.factory.UserFactory
import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class BalanceCommand : Command(CommandConfig(
        "balance",
        "Get your balance.",
        Category.GAMBLING,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        return e.message.userMentions
                .collectList()
                .flatMap { mentions ->
                    if (mentions.isNotEmpty()) {
                        val id = mentions[0].id.asLong()

                        e.sendMessage(container, "other", mentions[0].username, UserFactory.getOrCreate(id).bal)
                    } else {
                        val id = e.message.author.get().id.asLong()

                        e.sendMessage(container, "self", UserFactory.getOrCreate(id).bal)
                    }
                }
                .then()
    }
}