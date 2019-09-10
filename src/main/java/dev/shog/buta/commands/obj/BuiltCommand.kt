package dev.shog.buta.commands.obj

import dev.shog.buta.util.update
import discord4j.core.event.domain.message.MessageCreateEvent

/**
 * A built [Command].
 */
abstract class BuiltCommand {
    /**
     * The command's [CommandMeta].
     */
    abstract val meta: CommandMeta

    /**
     * When the command is invoked by a user.
     */
    abstract suspend fun invoke(e: MessageCreateEvent, args: MutableList<String>)

    /**
     * When the command's help command is invoked by a user.
     */
    fun invokeHelp(e: MessageCreateEvent) {
        e.message.channel.subscribe {
            it.createEmbed { msg ->
                msg.update(e.message.author.get())

                msg.setTitle("Help : ${meta.commandName}")
                msg.setDescription(meta.commandDesc)

                meta.commands.entries.forEach { pair ->
                    msg.addField(pair.key, pair.value, true)
                }
            }.subscribe()
        }
    }
}