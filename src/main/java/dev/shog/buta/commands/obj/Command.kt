package dev.shog.buta.commands.obj

import dev.shog.buta.EN_US
import dev.shog.buta.commands.permission.Permable
import dev.shog.buta.commands.permission.PermissionFactory
import discord4j.core.event.domain.message.MessageCreateEvent
import org.json.JSONObject
import reactor.core.publisher.Mono

/**
 * A command.
 */
data class Command(
        val name: String,
        val category: Categories,
        val isPmAvailable: Boolean = true,
        val permable: Permable = PermissionFactory.hasPermission(),
        val invoke: (MessageCreateEvent, MutableList<String>, JSONObject) -> Mono<Void>
) {
    /**
     * Build an [Command]
     */
    fun build() = object : ICommand(LangFillableContent.getFromCommandName(name), isPmAvailable, category, permable) {
        override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<Void> =
                invoke.invoke(e, args, EN_US.getJSONObject(name).getJSONObject("response"))
    }
}