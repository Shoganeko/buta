package dev.shog.buta.api.obj

import dev.shog.buta.api.factory.UserFactory

/**
 * A user object.
 */
class User(override val id: Long, bal: Long, lastDailyReward: Long, xp: Long, scoreData: String) : ButaObject {
    /**
     * Set the [bal] in the object and in the database.
     */
    var bal: Long = bal
        set(value) {
            field = value

            UserFactory.updateObject(this, "bal" to value)
        }

    /**
     * Set the [lastDailyReward] in the object and in the database.
     */
    var lastDailyReward: Long = lastDailyReward
        set(value) {
            field = value

            UserFactory.updateObject(this, "last_daily_reward" to value)
        }

    /**
     * Set the [xp] in the object and in the database.
     */
    var xp: Long = xp
        set(value) {
            field = value

            UserFactory.updateObject(this, "xp" to value)
        }

    /**
     * Set the [scoreData] in the object and in the database.
     *
     * This should be a JSON array.
     */
    var scoreData: String = scoreData
        set(value) {
            field = value

            UserFactory.updateObject(this, "score_data" to value)
        }
}