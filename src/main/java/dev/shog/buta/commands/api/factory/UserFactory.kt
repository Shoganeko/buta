package dev.shog.buta.commands.api.factory

import dev.shog.buta.commands.api.Api
import dev.shog.buta.commands.api.obj.ButaObject
import dev.shog.buta.commands.api.obj.User
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

object UserFactory : ButaFactory<User>() {
    override fun updateObject(id: Long, obj: ButaObject): Mono<Void> {
        assert(id == obj.id && obj is User)

        cache[id] = obj as User

        return Api.updateUserObject(obj)
    }

    override fun createObject(id: Long): Mono<Void> {
        val user = User().apply { this.id = id }

        cache[id] = user

        return Api.uploadUserObject(user)
    }

    override fun deleteObject(id: Long): Mono<Void> {
        cache.remove(id)

        return Api.deleteUserObject(id)
    }

    override fun getObject(id: Long): Mono<User> {
        val ch = cache[id]

        return if (ch != null && ch as? User != null)
            ch.toMono()
        else Api.getUserObject(id)
                .doOnNext { user -> cache[id] = user }
    }
}