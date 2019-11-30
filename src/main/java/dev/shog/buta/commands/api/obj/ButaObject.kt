package dev.shog.buta.commands.api.obj

import java.io.Serializable

/**
 * A buta object.
 */
interface ButaObject : Serializable {
    /** The ID of the Buta object. */
    var id: Long

    /** The type of the Buta object. */
    var type: Int

    /** If the buta object is invalid. */
    fun isInvalid(): Boolean =
            0 > id

    companion object {
        /** Get an empty [ButaObject].*/
        fun getEmpty(): ButaObject = object : ButaObject {
            override var type: Int = -1
            override var id: Long = 0
        }
    }
}