package dev.shog.buta.commands.api.factory

import dev.shog.buta.commands.api.Api
import dev.shog.buta.commands.api.obj.ButaObject
import dev.shog.buta.commands.api.obj.Guild
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

object GuildFactory : ButaFactory<Guild>() {
    override fun updateObject(id: Long, obj: ButaObject): Mono<Void> {
        assert(id == obj.id && obj is Guild)

        cache[id] = obj as Guild

        return Api.updateGuildObject(obj)
    }

    override fun createObject(id: Long): Mono<Void> {
        if (cache.contains(id))
            return Mono.error(Exception("Trying to create an object that already exists!"))

        val guild = Guild().apply { this.id = id }

        cache[id] = guild

        return Api.uploadGuildObject(guild)
    }

    override fun deleteObject(id: Long): Mono<Void> {
        cache.remove(id)

        return Api.deleteGuildObject(id)
    }

    override fun getObject(id: Long): Mono<Guild> {
        val ch = cache[id]

        return if (ch != null && ch as? Guild != null)
            ch.toMono()
        else Api.getGuildObject(id)
                .doOnNext { guild -> cache[id] = guild }
    }
}