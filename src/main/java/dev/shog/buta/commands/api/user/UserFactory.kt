package dev.shog.buta.commands.api.user

import dev.shog.buta.commands.api.API
import discord4j.core.`object`.util.Snowflake
import org.json.JSONObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * A [User] factory
 */
object UserFactory {
    val USERS = ConcurrentHashMap<Long, User>()

    /**
     * Gets a [User].
     */
    fun getUser(long: Long): Mono<User> =
            Flux.fromIterable(USERS.entries)
                    .filter { p -> p.key == long }
                    .map { p -> p.value }
                    .collectList()
                    .flatMap { if (it.size >= 1) Mono.just(it[0]) else getUserFromMojura(long) }

    /**
     * Gets a user from Mojura.
     */
    private fun getUserFromMojura(long: Long): Mono<User> =
            API.getJsonObject(API.getObject(API.Type.USER, long))
                    .flatMap { obj -> toUserObject(obj, long) }
                    .doOnNext { g -> USERS[long] = g }
    /**
     * Gets a [User].
     */
    fun getUser(snowflake: Snowflake): Mono<User> = getUser(snowflake.asLong())

    /**
     * Creates a [User].
     */
    private fun createUser(long: Long): Mono<User> =
            API.getJsonObject(API.createObject(API.Type.USER, long))
                    .flatMap { obj -> toUserObject(obj, long) }

    /**
     * Turns a [JSONObject] into a [User]
     */
    private fun toUserObject(jsonObject: JSONObject, long: Long): Mono<User> =
            Mono.just(User(long))
                    .doOnNext { obj ->
                        Flux.fromIterable(jsonObject.toMap().entries)
                                .subscribe { i ->
                                    obj.data[i.key] = i.value
                                }
                    }
}