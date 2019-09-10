package dev.shog.buta.events

import dev.shog.buta.LOGGER
import dev.shog.buta.commands.UserThreadHandler
import dev.shog.buta.commands.obj.BuiltCommand
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.events.obj.Event
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A message event.
 * It's also a coroutine scope, allowing for a thread for each new message.
 */
object MessageEvent : Event(), CoroutineScope by CoroutineScope(Dispatchers.Unconfined) {
    override fun invoke(event: discord4j.core.event.domain.Event) {
        require(event is MessageCreateEvent)

        launch {
            if (event.message.author.isPresent && !event.message.author.get().isBot && UserThreadHandler.can(event.message.author.get())) {
                LOGGER.debug("Starting task for ${event.message.author.get().username}...")

                val isGuild = event.guildId.isPresent
                val command = getCommandFromMessage(event.message, "!") // TODO Prefix

                if (command != null) {
                    if (!command.meta.isPmAvailable && !isGuild) {
                        LOGGER.debug("${event.message.author.get().username} tried a command that wasn't available in a non-guild environment!")

                        event.message.channel
                                .flatMap { ch ->
                                    ch.createMessage("This command isn't available in a non-guild environment!")
                                }.subscribe()
                    } else {
                        val args = event.message.content.get().split(" ").toMutableList()
                        args.removeAt(0)

                        LOGGER.debug("${event.message.author.get().username} successfully invoked a command!")

                        command.invoke(event, args)
                    }
                } else {
                    LOGGER.debug("${event.message.author.get().username} attempted an invalid command! Command = {}", event.message.content.get())
                }

                LOGGER.debug("Finished task for ${event.message.author.get().username}")
                UserThreadHandler.finish(event.message.author.get())
            }
        }
    }

    /**
     * Gets a command from [msg], with [prefix].
     */
    private fun getCommandFromMessage(msg: Message, prefix: String): BuiltCommand? {
        if (!msg.content.isPresent)
            return null

        if (msg.content.get().startsWith(prefix, ignoreCase = true)) {
            val args = msg.content.get().split(" ").toMutableList()

            args[0].removePrefix(prefix).also {
                synchronized(Command.COMMANDS) {
                    for (command in Command.COMMANDS) {
                        if (command.meta.commandName.equals(it, ignoreCase = true))
                            return command
                    }
                }
            }
        }

        return null
    }
}