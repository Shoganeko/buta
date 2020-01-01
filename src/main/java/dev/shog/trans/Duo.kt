package dev.shog.trans

/**
 * This is incredibly similar to [Pair].
 * However, this can be initialized with keeping variables as null, allowing for transport through web.
 */

class Duo<T, R>(val first: T? = null, val second: R? = null)

infix fun <T, R> T.duo(r: R): Duo<T, R> =
        Duo(this, r)