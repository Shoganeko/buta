package dev.shog.buta.commands.obj

import dev.shog.buta.commands.permission.Permable
import dev.shog.buta.util.update
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/**
 * The main command interface.
 *
 * The command can be invoked by a user sending a message with the prefix then [commandName] in a channel where Buta can see it.
 *
 * If [isPmAvailable] is true, it can be accessible through pms.
 */
abstract class Command(
        val commandName: String,
        val commandDesc: String,
        val helpCommand: HashMap<String, String>,
        val isPmAvailable: Boolean,
        val category: Categories,
        val permable: Permable,
        val alias: ArrayList<String>
) {
    /**
     * When the command is invoked by a user.
     */
    abstract fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<Void>

    /**
     * When the command's help command is invoked by a user.
     */
    fun invokeHelp(e: MessageCreateEvent): Mono<Void> =
            e.message.channel
                    .flatMap {
                        it.createEmbed { msg ->
                            msg.update(e.message.author.get())

                            msg.setTitle("Help : $commandName")
                            msg.setDescription(commandDesc)

                            helpCommand.entries.forEach { pair ->
                                msg.addField(pair.key, pair.value, true)
                            }
                        }
                    }.then()

    companion object {
        /**
         * All of the commands.
         */
        val COMMANDS = ArrayList<Command>()
    }
}