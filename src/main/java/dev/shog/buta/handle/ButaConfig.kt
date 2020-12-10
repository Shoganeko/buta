package dev.shog.buta.handle

import dev.shog.lib.transport.Duo
import dev.shog.buta.APP

/**
 * The config for [APP].
 */
data class ButaConfig(
    val token: String? = "",
    val sqlUrl: String? = "",
    val sqlCredentials: Duo<String, String>? = Duo("username", "password"),
    val webhook: String? = "",
    val stocks: String? = "",
    val mongoPass: String? = ""
)