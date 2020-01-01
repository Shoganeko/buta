package dev.shog.buta.commands.api.obj

/**
 * A guild object.
 */
class Guild : ButaObject {
    override var type: Int = 1
    override var id: Long = 0

    var prefix: String = "b!"
    var joinMessage: Pair<Boolean, String> = false to "{user} has joined {guild-name}!"
    var leaveMessage: Pair<Boolean, String> = false to "{user} has joined {guild-name}!"
    var joinRole: Pair<Boolean, Long> = false to -1L
    var swearFilter: Pair<Boolean, String> = false to "{user} you cannot swear!"
    var disabledCategories: Pair<Boolean, ArrayList<String>> = false to arrayListOf()
}