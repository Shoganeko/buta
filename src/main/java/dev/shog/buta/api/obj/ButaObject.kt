package dev.shog.buta.api.obj

import java.io.Serializable

/**
 * A buta object.
 */
interface ButaObject : Serializable {
    /**
     * The ID of the Buta object.
     */
    val id: Long
}