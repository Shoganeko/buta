package dev.shog.buta.commands

import dev.shog.buta.LOGGER
import discord4j.core.`object`.entity.User
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles user's thread limit
 */
object UserThreadHandler {

    /**
     * Pairs of [User]s and their amount of running tasks.
     */
    val users = ConcurrentHashMap<User, ArrayList<String>>()

    /**
     * If a [user] can run a command without going over their limit.
     */
    fun can(user: User, identifier: String): Boolean {
        users[user].also {
            return if (it != null) {
                if (it.size >= 3) {
                    LOGGER.error("${user.username} is at 3 threads!")
                    false
                } else {
                    users[user]?.add(identifier.toLowerCase())

                    true
                }
            } else {
                users[user] = arrayListOf()

                true
            }
        }
    }

    /**
     * Finishes a task for a [user], and removes 1 allowing them more access.
     */
    fun finish(user: User, identifier: String) {
        users[user].also {
            if (it == null) {
                users[user] = arrayListOf()
            } else if (it.size != 0) {
                users[user]?.remove(identifier.toLowerCase())
            }
        }
    }
}