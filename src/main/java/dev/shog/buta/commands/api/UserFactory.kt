package dev.shog.buta.commands.api

import dev.shog.buta.commands.api.obj.User
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.util.concurrent.ConcurrentHashMap

/**
 * Creates and manages [User]s.
 */
object UserFactory {
    /**
     * Stores users.
     */
    private val cache = ConcurrentHashMap<Long, User>()

    /**
     * Update [id] with new [user] data.
     *
     * @param id The ID of the user.
     * @param user The new user data.
     */
    fun update(id: Long, user: User): Mono<Void> {
        if (id != user.id)
            return Mono.error(Exception("Invalid user object!"))

        return Api.updateUserObject(user)
    }

    /**
     * Create a user with the [id].
     *
     * @param id The ID of the object to create.
     */
    fun create(id: Long): Mono<Void> =
            User()
                    .toMono()
                    .doOnNext { user -> user.id = id }
                    .doOnNext { user -> cache[id] = user }
                    .flatMap { user -> Api.uploadUserObject(user) }

    /**
     * Delete [id]
     *
     * @param id The object to delete
     */
    fun delete(id: Long): Mono<Void> =
            Api.deleteUserObject(id)

    /**
     * Use [get] to see if the retrieved object does exist.
     *
     * @param id The object to check if exists.
     */
    fun exists(id: Long): Mono<Boolean> =
            get(id)
                    .map { obj -> !obj.isInvalid() }

    /**
     * Attempt to get [id] from the cache.
     * If it's not in the cache, use the [Api] to retrieve it from Mojor.
     *
     * @param id The object's ID to retrieve.
     */
    fun get(id: Long): Mono<User> =
            cache[id]?.toMono()
                    ?: Api.getUserObject(id)
                            .doOnNext { obj -> if (!obj.isInvalid()) cache[id] = obj }
}