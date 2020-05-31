package dev.shog.buta.handle

import dev.shog.buta.APP
import java.sql.Connection
import java.sql.DriverManager

/**
 * The SQL manager.
 */
object PostgreSql {
    private val connection: Connection

    private val URL: String
    private val USERNAME: String
    private val PASSWORD: String

    init {
        val cfg = APP.getConfigObject<ButaConfig>()

        URL = cfg.sqlUrl!!
        USERNAME = cfg.sqlCredentials?.first!!
        PASSWORD = cfg.sqlCredentials.second!!

        Class.forName("org.postgresql.Driver")
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)
    }

    /**
     * Create a connection to AWS.
     */
    fun getConnection(): Connection =
            connection
}