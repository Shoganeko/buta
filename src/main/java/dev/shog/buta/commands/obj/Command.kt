package dev.shog.buta.commands.obj

import dev.shog.buta.EN_US
import dev.shog.buta.commands.permission.Permable
import dev.shog.buta.commands.permission.PermissionFactory
import discord4j.core.event.domain.message.MessageCreateEvent
import org.json.JSONObject
import reactor.core.publisher.Mono

/**
 * A command.
 *
 * @param name The name of the command.
 * @param category The command's category.
 * @param isPmAvailable If the command can be used through PM.
 * @param permable The command's permable.
 * @param invoke What should happen when the command is invoked.
 */
data class Command(
        val name: String,
        val category: Categories,
        val isPmAvailable: Boolean = true,
        val permable: Permable = PermissionFactory.hasPermission(),
        val invoke: (MessageCreateEvent, MutableList<String>, JSONObject) -> Mono<*>
) {
    /**
     * Build an [Command]
     */
    fun build() = object : ICommand(LangFillableContent.getFromCommandName(name), isPmAvailable, category, permable) {
        override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> =
                invoke.invoke(e, args, EN_US.getJSONObject(name).getJSONObject("response"))
    }
}