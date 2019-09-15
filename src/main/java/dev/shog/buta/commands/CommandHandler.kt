package dev.shog.buta.commands

import dev.shog.buta.LOGGER
import discord4j.core.`object`.entity.User
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles user's thread limit.
 */
object UserThreadHandler {
    /**
     * Pairs of [User]s and their amount of running tasks.
     */
    private val users = ConcurrentHashMap<User, Int>()

    /**
     * If a [user] can run a command without going over their limit.
     */
    fun can(user: User): Boolean {
        users[user].also {
            return if (it != null) {
                if (it >= 3) {
                    LOGGER.error("${user.username} is at 3 threads!")
                    false
                } else {
                    users[user] = it + 1

                    true
                }
            } else {
                users[user] = 0

                true
            }
        }
    }

    /**
     * Finishes a task for a [user], and removes 1 allowing them more access.
     */
    fun finish(user: User) {
        users[user].also {
            if (it == null) {
                users[user] = 0
            } else if (it != 0) {
                users[user] = it - 1
            }
        }
    }
}