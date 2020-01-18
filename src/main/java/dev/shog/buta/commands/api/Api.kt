package dev.shog.buta.commands.api

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.buta.APP
import dev.shog.buta.PRODUCTION
import dev.shog.buta.commands.api.obj.Guild
import dev.shog.buta.commands.api.obj.User
import dev.shog.buta.handle.ButaConfig
import dev.shog.buta.util.logRequest
import dev.shog.lib.token.TokenManager
import dev.shog.lib.util.eitherOr
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import kong.unirest.Unirest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * The Mojor/Buta API
 */
object Api {
    /**
     * The base URL for making request
     */
    private val BASE_URL: String = getBaseUrl()

    /**
     * The token manager
     */
    private val tokenManager by lazy {
        val cfg = APP.getConfigObject<ButaConfig>()
        TokenManager(cfg.creds?.first ?: "", cfg.creds?.second ?: "", "buta")
    }

    /**
     * Refresh the presences Mojor-side.
     *
     * @return If the request was successful.
     */
    fun refreshPresences(): Mono<Boolean> =
            Unirest.post("https://api.shog.dev/v2/buta/presences")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asStringAsync()
                    .toMono()
                    .logRequest("POST", "/v2/buta/presences")
                    .map { req -> req.isSuccess }

    /**
     * Get presences from mojor
     */
    fun getPresences(): Flux<Presence> =
            Unirest.get("https://api.shog.dev/v2/buta/presences")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asJsonAsync()
                    .toMono()
                    .logRequest("GET", "/v2/buta/presences")
                    .map { js -> js.body.array }
                    .flatMapIterable { ar -> ar.toMutableList() }
                    .map { any ->
                        val obj = any as kong.unirest.json.JSONObject

                        val activity = when (obj.getInt("activityType")) {
                            1 -> Activity.playing(obj.getString("statusText"))
                            2 -> Activity.watching(obj.getString("statusText"))
                            3 -> Activity.listening(obj.getString("statusText"))

                            else -> Activity.playing("")
                        }

                        when (obj.getInt("statusType")) {
                            1 -> Presence.online(activity)
                            2 -> Presence.invisible()
                            3 -> Presence.idle(activity)
                            4 -> Presence.doNotDisturb(activity)
                            else -> Presence.invisible()
                        }
                    }

    /** Get the base URL */
    private fun getBaseUrl(): String {
        val cfg = APP.getConfigObject<ButaConfig>()

        val prod = cfg.api?.second ?: ""
        val dev = cfg.api?.first ?: ""

        return PRODUCTION.eitherOr(prod, dev)
    }

    /**
     * Get a guild object by their [id].
     */
    fun getGuildObject(id: Long): Mono<Guild> =
            Unirest.get("$BASE_URL/v2/buta/$id/1")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asJsonAsync()
                    .toMono()
                    .logRequest("GET", "/v2/buta/$id/1")
                    .filter { req -> req.isSuccess }
                    .map { req -> req.body.`object`.getJSONObject("payload") }
                    .map { payload -> ObjectMapper().readValue(payload.toString(), Guild::class.java) }

    /**
     * Get a user object by their [id].
     */
    fun getUserObject(id: Long): Mono<User> =
            Unirest.get("$BASE_URL/v2/buta/$id/2")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asJsonAsync()
                    .toMono()
                    .logRequest("GET", "/v2/buta/$id/2")
                    .filter { obj -> obj.isSuccess }
                    .map { req -> req.body.`object`.getJSONObject("payload") }
                    .map { payload -> ObjectMapper().readValue(payload.toString(), User::class.java) }

    /**
     * Upload a [user] object to the database.
     */
    fun uploadUserObject(user: User): Mono<Void> =
            Unirest.put("$BASE_URL/v2/buta/${user.id}/2")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .header("Content-Type", "application/json")
                    .body(user)
                    .asJsonAsync()
                    .toMono()
                    .logRequest("PUT", "/v2/buta/${user.id}/2")
                    .then()

    /**
     * Upload a [guild] object to the database.
     */
    fun uploadGuildObject(guild: Guild): Mono<Void> =
            Unirest.put("$BASE_URL/v2/buta/${guild.id}/1")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .header("Content-Type", "application/json")
                    .body(guild)
                    .asJsonAsync()
                    .toMono()
                    .logRequest("PUT", "/v2/buta/${guild.id}/1")
                    .then()

    /**
     * Update a [user] object to the database.
     */
    fun updateUserObject(user: User): Mono<Void> =
            Unirest.patch("$BASE_URL/v2/buta/${user.id}/2")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .header("Content-Type", "application/json")
                    .body(user)
                    .asJsonAsync()
                    .toMono()
                    .logRequest("PATCH", "/v2/buta/${user.id}/2")
                    .then()

    /**
     * Update a [guild] object to the database.
     */
    fun updateGuildObject(guild: Guild): Mono<Void> =
            Unirest.patch("$BASE_URL/v2/buta/${guild.id}/1")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .header("Content-Type", "application/json")
                    .body(guild)
                    .asJsonAsync()
                    .toMono()
                    .logRequest("PATCH", "/v2/buta/${guild.id}/1")
                    .then()

    /**
     * Delete a [id] object from the database.
     */
    fun deleteUserObject(id: Long): Mono<Void> =
            Unirest.delete("$BASE_URL/v2/buta/$id/2")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asJsonAsync()
                    .toMono()
                    .logRequest("DELETE", "/v2/buta/$id/2")
                    .then()

    /**
     * Delete a [id] object from the database.
     */
    fun deleteGuildObject(id: Long): Mono<Void> =
            Unirest.delete("$BASE_URL/v2/buta/$id/1")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asJsonAsync()
                    .toMono()
                    .logRequest("DELETE", "/v2/buta/$id/1")
                    .then()
}