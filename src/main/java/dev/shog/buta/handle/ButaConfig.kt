package dev.shog.buta.handle

import dev.shog.lib.transport.Duo

/**
 * The config for [APP].
 */
data class ButaConfig(
        val token: String? = "",
        val api: Duo<String, String>? = Duo("production", "dev"),
        val webhook: String? = "",
        val creds: Duo<String, String>? = Duo("username", "password")
)