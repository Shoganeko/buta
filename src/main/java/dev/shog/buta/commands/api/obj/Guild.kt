package dev.shog.buta.commands.api.obj

/**
 * A guild object.
 */
class Guild : ButaObject {
    override var type: Int = 1
    override var id: Long = 0

    var prefix: String = "b!"
}