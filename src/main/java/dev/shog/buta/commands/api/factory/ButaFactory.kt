package dev.shog.buta.commands.api.factory

import dev.shog.buta.commands.api.obj.ButaObject
import dev.shog.buta.commands.api.obj.User
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * Creates and manages [User]s.
 */
abstract class ButaFactory<T> {
    val cache = ConcurrentHashMap<Long, ButaObject>()

    abstract fun updateObject(id: Long, obj: ButaObject): Mono<Void>

    abstract fun createObject(id: Long): Mono<Void>

    abstract fun deleteObject(id: Long): Mono<Void>

    fun objectExists(id: Long): Mono<Boolean> =
            getObject(id).hasElement()

    abstract fun getObject(id: Long): Mono<T>
}