package dev.shog.buta.commands

import dev.shog.buta.EN_US
import dev.shog.buta.commands.obj.*
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/**
 * Gets a command, and turns it into a [Command].
 */
object CommandFactory {
    /**
     * Builds the command.
     *
     * @throws Exception An invalid command as [cmd].
     */
    fun build(cmd: Any): Command {
        when (cmd) {
            // Turns a InfoCommand into a Command.
            is InfoCommand -> {
                return object : Command(LangFillableContent.getFromCommandName(cmd.name), cmd.isPmAvailable, Categories.INFO, cmd.permable) {
                    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<Void> =
                            cmd.invoke.invoke(Pair(e, args), EN_US.get().getJSONObject(cmd.name).getJSONObject("response"))
                }
            }
        }

        throw Exception("Invalid Command Inputted!")
    }
}