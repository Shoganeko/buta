package dev.shog.buta.commands.api

import dev.shog.buta.FileHandler
import dev.shog.buta.commands.api.obj.Guild
import dev.shog.buta.commands.api.token.TokenManager
import dev.shog.buta.util.swap
import dev.shog.buta.commands.api.obj.User
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import kong.unirest.Unirest
import org.json.JSONObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import kotlin.system.exitProcess

/**
 * The Mojor/Buta API
 */
object Api {
    /**
     * The base URL for making request
     */
    val BASE_URL: String = getBaseUrl()

    /**
     * The token manager
     */
    private val tokenManager = TokenManager()

    /**
     * Refresh the presences Mojor-side.
     *
     * @return If the request was successful.
     */
    fun refreshPresences(): Mono<Boolean> =
            Unirest.post("https://api.shog.dev/v2/buta/presences")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asEmptyAsync()
                    .toMono()
                    .map { request -> request.isSuccess }

    /**
     * Get presences from mojor
     */
    fun getPresences(): Flux<Presence> =
            Unirest.get("https://api.shog.dev/v2/buta/presences")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asJsonAsync()
                    .toMono()
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
        val obj = try {
            JSONObject(FileHandler.get("api-base-url").toString())
        } catch (e: Exception) {
            exitProcess(-1)
        }

        val prod = obj.getString("prod")
        val dev = obj.getString("dev")

        return swap(prod, dev)
    }

    /**
     * Get a guild object by their [id].
     */
    fun getGuildObject(id: Long): Mono<Guild> =
            Unirest.get("$BASE_URL/v2/buta/$id/1")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asObjectAsync(Guild::class.java)
                    .toMono()
                    .filter { obj -> obj.isSuccess }
                    .map { obj -> obj.body }

    /**
     * Get a user object by their [id].
     */
    fun getUserObject(id: Long): Mono<User> =
            Unirest.get("$BASE_URL/v2/buta/$id/2")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asObjectAsync(User::class.java)
                    .toMono()
                    .filter { obj -> obj.isSuccess }
                    .map { obj -> obj.body }

    /**
     * Upload a [user] object to the database.
     */
    fun uploadUserObject(user: User): Mono<Void> =
            Unirest.put("$BASE_URL/v2/buta/${user.id}/2")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .header("Content-Type", "application/json")
                    .body(user)
                    .asEmptyAsync()
                    .toMono()
                    .then()

    /**
     * Upload a [guild] object to the database.
     */
    fun uploadGuildObject(guild: Guild): Mono<Void> =
            Unirest.put("$BASE_URL/v2/buta/${guild.id}/1")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .header("Content-Type", "application/json")
                    .body(guild)
                    .asEmptyAsync()
                    .toMono()
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
                    .then()

    /**
     * Delete a [id] object from the database.
     */
    fun deleteUserObject(id: Long): Mono<Void> =
            Unirest.put("$BASE_URL/v2/buta/$id/2")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asJsonAsync()
                    .toMono()
                    .then()

    /**
     * Delete a [id] object from the database.
     */
    fun deleteGuildObject(id: Long): Mono<Void> =
            Unirest.delete("$BASE_URL/v2/buta/$id/1")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asJsonAsync()
                    .toMono()
                    .then()
}