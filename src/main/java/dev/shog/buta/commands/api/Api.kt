package dev.shog.buta.commands.api

import dev.shog.buta.FileHandler
import dev.shog.buta.commands.api.obj.Guild
import dev.shog.buta.commands.api.token.TokenManager
import dev.shog.buta.util.swap
import dev.shog.buta.commands.api.obj.User
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
     *  TODO Get all presences from Mojor.
     */
    fun getPresences(): Flux<Presence> =
            Flux.just(Presence.idle())

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
                    .map { obj -> obj.body }

    /**
     * Get a user object by their [id].
     */
    fun getUserObject(id: Long): Mono<User> =
            Unirest.get("$BASE_URL/v2/buta/$id/2")
                    .header("Authorization", "token ${tokenManager.getProperToken()}")
                    .asObjectAsync(User::class.java)
                    .toMono()
                    .map { obj -> if (obj.isSuccess) obj.body else User().apply { this.id = -1 } }

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
                    .then()

    /**
     * Update a [user] object to the database.
     */
    fun updateUserObject(user: User): Mono<Void> =
            Unirest.put("$BASE_URL/v2/buta/${user.id}/2")
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