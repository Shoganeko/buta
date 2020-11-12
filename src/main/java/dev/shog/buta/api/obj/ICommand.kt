package dev.shog.buta.api.obj

import com.gitlab.kordlib.core.event.message.MessageCreateEvent

/**
 * The main command interface.
 */
interface ICommand {
    /**
     * When the command is invoked by a user.
     */
    suspend fun invoke(e: MessageCreateEvent, args: MutableList<String>)

    /**
     * When the help command is invoked by a user.
     */
    suspend fun help(e: MessageCreateEvent)
}