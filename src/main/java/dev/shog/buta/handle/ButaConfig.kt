package dev.shog.buta.handle

import dev.shog.lib.transport.Duo
import dev.shog.buta.APP

/**
 * The config for [APP].
 *
 * @param token The bot's token.
 * @param sqlUrl The SQL URL.
 * @param sqlCredentials The credentials to SQL.
 * @param webhook The Discord webhook
 * @param stocks The stocks API key
 */
data class ButaConfig(
        val token: String? = "",
        val sqlUrl: String? = "",
        val sqlCredentials: Duo<String, String>? = Duo("username", "password"),
        val webhook: String? = "",
        val stocks: String? = ""
)