package dev.shog.buta.commands.api.guild

import dev.shog.buta.LOGGER
import dev.shog.buta.commands.api.API
import discord4j.core.`object`.util.Snowflake
import org.json.JSONObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.Exception
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * A [Guild] factory
 */
object GuildFactory {
    val GUILDS = ConcurrentHashMap<Long, Guild>()

    /**
     * Gets a [Guild].
     */
    fun getGuild(long: Long): Mono<Guild> =
            Flux.fromIterable(GUILDS.entries)
                    .filter { p -> p.key == long }
                    .map { p -> p.value }
                    .collectList()
                    .flatMap { if (it.size >= 1) Mono.just(it[0]) else getGuildFromMojura(long) }

    /**
     * Gets a guild from Mojura.
     */
    private fun getGuildFromMojura(long: Long): Mono<Guild> =
            API.getJsonObject(API.getObject(API.Type.GUILD, long))
                    .flatMap { obj -> toGuildObject(obj, long) }
                    .doOnNext { g -> GUILDS[long] = g }


    /**
     * Gets a [Guild].
     */
    fun getGuild(snowflake: Snowflake): Mono<Guild> = getGuild(snowflake.asLong())

    /**
     * Creates a [Guild].
     */
    private fun createGuild(long: Long): Mono<Guild> =
            API.getJsonObject(API.createObject(API.Type.GUILD, long))
                    .flatMap { obj -> toGuildObject(obj, long) }

    /**
     * Turns a [JSONObject] into a [Guild]
     */
    private fun toGuildObject(jsonObject: JSONObject, long: Long): Mono<Guild> =
            Mono.just(Guild(long))
                    .doOnNext { obj ->
                        Flux.fromIterable(jsonObject.toMap().entries)
                                .subscribe { i ->
                                    obj.data[i.key] = i.value
                                }
                    }
}