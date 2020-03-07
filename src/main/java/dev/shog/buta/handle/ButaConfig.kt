package dev.shog.buta.handle

import dev.shog.lib.transport.Duo
import dev.shog.buta.APP

/**
 * The config for [APP].
 *
 * @param token The bot's token.
 * @param api The API url for production and dev.
 * @param webhook The Discord webhook
 * @param creds The API credentials.
 * @param stocks The stocks API key
 */
data class ButaConfig(
        val token: String? = "",
        val api: Duo<String, String>? = Duo("production", "dev"),
        val webhook: String? = "",
        val creds: Duo<String, String>? = Duo("username", "password"),
        val stocks: String? = ""
)