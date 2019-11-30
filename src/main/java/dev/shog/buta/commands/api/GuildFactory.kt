package dev.shog.buta.commands.api

import dev.shog.buta.commands.api.obj.Guild
import dev.shog.buta.commands.api.obj.User
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.util.concurrent.ConcurrentHashMap

/**
 * Creates and manages [Guild]s.
 */
object GuildFactory {
    /**
     * Stores guilds.
     */
    private val cache = ConcurrentHashMap<Long, Guild>()

    /**
     * Update [id] with new [guild] data.
     *
     * @param id The ID of the user.
     * @param guild The new guild data.
     */
    fun update(id: Long, guild: Guild): Mono<Void> {
        if (id != guild.id)
            return Mono.error(Exception("Invalid user object!"))

        return Api.updateGuildObject(guild)
    }

    /**
     * Create a guild with the [id].
     *
     * @param id The ID of the object to create.
     */
    fun create(id: Long): Mono<Void> =
            Guild()
                    .toMono()
                    .doOnNext { guild -> guild.id = id }
                    .doOnNext { guild -> cache[id] = guild }
                    .flatMap { guild -> Api.uploadGuildObject(guild) }

    /**
     * Create a guild, then retrieve that created guild.
     *
     * @param id The ID of the guild to create.
     */
    fun createAndGet(id: Long): Mono<Guild> =
            create(id)
                    .then(get(id))

    /**
     * Use [get] to see if the retrieved object does exist.
     *
     * @param id The object to check if exists.
     */
    fun exists(id: Long): Mono<Boolean> =
            get(id)
                    .map { obj -> !obj.isInvalid() }

    /**
     * Delete [id]
     *
     * @param id The object to delete
     */
    fun delete(id: Long): Mono<Void> =
            Api.deleteGuildObject(id)

    /**
     * Attempt to get [id] from the cache.
     * If it's not in the cache, use the [Api] to retrieve it from Mojor.
     *
     * @param id The object's ID to retrieve.
     */
    fun get(id: Long): Mono<Guild> =
            cache[id]?.toMono()
                    ?: Api.getGuildObject(id)
                            .onErrorResume { Mono.empty<Guild>() }
                            .flatMap { obj -> if (obj.isInvalid()) Mono.empty<Guild>() else obj.toMono() }
                            .doOnNext { obj -> if (!obj.isInvalid()) cache[id] = obj }
}