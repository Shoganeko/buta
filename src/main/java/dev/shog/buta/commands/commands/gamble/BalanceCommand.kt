package dev.shog.buta.commands.commands.gamble

import dev.shog.buta.api.factory.UserFactory
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage

val BALANCE_COMMAND = Command(CommandConfig("balance")) {
    event.message.userMentions
            .collectList()
            .flatMap { mentions ->
                if (mentions.isNotEmpty()) {
                    val id = mentions[0].id.asLong()

                    sendMessage("other", mentions[0].username, UserFactory.getOrCreate(id).bal)
                } else {
                    val id = event.message.author.get().id.asLong()

                    sendMessage("self", UserFactory.getOrCreate(id).bal)
                }
            }
            .then()
}