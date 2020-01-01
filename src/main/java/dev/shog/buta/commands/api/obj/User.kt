package dev.shog.buta.commands.api.obj

/**
 * A user object.
 */
class User : ButaObject {
    override var type: Int = 2
    override var id: Long = 0

    var bal: Long = 0L
    var lastDailyReward: Long = -1
    var xp: Long = -1
}