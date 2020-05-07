package dev.shog.buta.commands.api.obj

import dev.shog.buta.commands.api.factory.GuildFactory
import dev.shog.lib.util.eitherOr

/**
 * A user object.
 */
class Guild(
        override val id: Long,
        prefix: String,
        joinMessage: String,
        leaveMessage: String,
        joinRole: Long,
        swearFilterMsg: String,
        swearFilterOn: Boolean,
        disabledCategories: String
) : ButaObject {
    var prefix: String = prefix
        set(value) {
            field = value

            GuildFactory.updateObject(this, "prefix" to value)
        }

    /**
     * Set the [joinMessage] in the object and in the database.
     */
    var joinMessage: String = joinMessage
        set(value) {
            field = value

            GuildFactory.updateObject(this, "join_message" to value)
        }

    /**
     * If [joinMessage] enabled.
     */
    fun isJoinMessageEnabled(): Boolean =
            joinMessage != ""

    /**
     * Set the [leaveMessage] in the object and in the database.
     */
    var leaveMessage: String = leaveMessage
        set(value) {
            field = value

            GuildFactory.updateObject(this, "leaveMessage" to value)
        }

    /**
     * If [leaveMessage] is enabled.
     */
    fun isLeaveMessageEnabled(): Boolean =
            leaveMessage != ""

    /**
     * Set the [joinRole] in the object and in the database.
     */
    var joinRole: Long = joinRole
        set(value) {
            field = value

            GuildFactory.updateObject(this, "join_role" to value)
        }

    /**
     * If [joinRole] is enabled.
     */
    fun isJoinRoleEnabled(): Boolean =
            joinRole != -1L

    /**
     * Set the [swearFilterMsg] in the object and in the database.
     */
    var swearFilterMsg: String = swearFilterMsg
        set(value) {
            field = value

            GuildFactory.updateObject(this, "swear_filter_msg" to value)
        }

    /**
     * Set the [swearFilterOn] in the object and in the database.
     */
    var swearFilterOn: Boolean = swearFilterOn
        set(value) {
            field = value

            GuildFactory.updateObject(this, "swear_filter_on" to value.eitherOr(1, 0))
        }

    /**
     * Set [swearFilterMsg] and [swearFilterMsg].
     */
    fun setSwearFilter(enabled: Boolean, message: String) {
        swearFilterOn = enabled
        swearFilterMsg = message
    }

    /**
     * Set the [disabledCategories] in the object and in the database.
     */
    var disabledCategories: String = disabledCategories
        set(value) {
            field = value

            GuildFactory.updateObject(this, "disabled_categories" to value)
        }
}