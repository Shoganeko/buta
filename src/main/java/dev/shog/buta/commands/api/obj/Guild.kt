package dev.shog.buta.commands.api.obj

import dev.shog.lib.transport.Duo
import dev.shog.lib.transport.duo

/**
 * A guild object.
 */
class Guild : ButaObject {
    override var type: Int = 1
    override var id: Long = 0

    var prefix: String = "b!"
    var joinMessage: Duo<Boolean, String> = false duo "{user} has joined {guild-name}!"
    var leaveMessage: Duo<Boolean, String> = false duo "{user} has joined {guild-name}!"
    var joinRole: Duo<Boolean, Long> = false duo -1L
    var swearFilter: Duo<Boolean, String> = false duo "{user} you cannot swear!"
    var disabledCategories: Duo<Boolean, ArrayList<String>> = false duo arrayListOf()
}