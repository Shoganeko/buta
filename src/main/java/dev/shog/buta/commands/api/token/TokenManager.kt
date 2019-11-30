package dev.shog.buta.commands.api.token

import dev.shog.buta.FileHandler
import dev.shog.buta.LOGGER
import dev.shog.buta.commands.api.Api.BASE_URL
import dev.shog.buta.util.fatal
import kong.unirest.Unirest
import org.json.JSONObject
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.*
import java.time.Instant
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

/**
 * Manages a token and makes sure it's always active.
 */
class TokenManager {
    /** The actual token.*/
    private var token: Token? = null

    /** Get the actual token string. */
    fun getProperToken(): String =
            token?.token
                    ?: throw NullPointerException("Token is null!")

    init {
        val file = File(FileHandler.BUTA_DIR.path + "${File.separator}tokenCache")

        if (file.exists())
            token = getTokenFromFile(file, true)

        if (token == null) {
            val obj = try {
                JSONObject(FileHandler.get("creds").toString())
            } catch (e: Exception) {
                LOGGER.fatal("Failed to get credentials!").subscribe()
                exitProcess(-1)
            }

            createToken(obj.getString("username"), obj.getString("password")).block() // needs to block :(
        }
    }

    /** Get a token from [file]. */
    private fun getTokenFromFile(file: File, schedule: Boolean = true): Token? {
        val ois = ObjectInputStream(FileInputStream(file))
        val token = ois.readObject() as? Token
                ?: return null

        val time = token.expiresOn - System.currentTimeMillis()

        if (time > 0) {
            if (schedule)
                Timer().schedule(timerTask { renewToken().subscribe() }, token.expiresOn)

            LOGGER.debug("Found token in file!")
            return token
        }

        return null
    }

    /** If there wasn't a previously stored token, create one. */
    private fun createToken(username: String, password: String): Mono<Void> =
            Unirest.post("$BASE_URL/v1/user")
                    .field("username", username)
                    .field("password", password)
                    .asJsonAsync()
                    .toMono()
                    .doOnNext { req ->
                        val ob = req.body.`object`.getJSONObject("token")

                        if (req.isSuccess) {
                            val newExpire = Date.from(Instant.ofEpochMilli(ob.getLong("expiresOn")))

                            Timer().schedule(timerTask { renewToken().subscribe() }, newExpire)

                            LOGGER.debug("Successfully created token!")
                        } else {
                            LOGGER.fatal("Failed to create token, exited!").subscribe()
                            exitProcess(-1)
                        }
                    }
                    .doOnNext { req ->
                        val jsObj = req.body.`object`.getJSONObject("token")

                        val newToken = Token(
                                jsObj.getString("token"),
                                jsObj.getLong("owner"),
                                jsObj.getLong("createdOn"),
                                jsObj.getLong("expiresOn")
                        )

                        token = newToken
                        writeToken(newToken)
                    }
                    .then()

    /** Write token to the cache. */
    private fun writeToken(token: Token) {
        val oos = ObjectOutputStream(
                FileOutputStream(
                        File(FileHandler.BUTA_DIR.path + "${File.separator}tokenCache")
                )
        )

        oos.writeObject(token)
    }

    /** Renew [token]. */
    private fun renewToken(): Mono<Void> =
            Unirest.patch("$BASE_URL/v1/token")
                    .header("Authorization", "token $token")
                    .asJsonAsync()
                    .toMono()
                    .doOnNext { req ->
                        val obj = req.body.`object`

                        if (obj.getBoolean("successful") || !req.isSuccess) {
                            val newExpire = Date.from(Instant.ofEpochMilli(obj.getLong("newExpire")))

                            Timer().schedule(timerTask { renewToken().subscribe() }, newExpire)

                            LOGGER.debug("Successfully renewed token!")
                        } else {
                            LOGGER.fatal("Failed to create token, exited!").subscribe()
                            exitProcess(-1)
                        }
                    }
                    .then()

    companion object {
        /**
         * The amount of time it takes for the token to expire.
         */
        private const val TOKEN_EXPIRE = 1000 * 60 * 60 * 24 * 6
    }
}