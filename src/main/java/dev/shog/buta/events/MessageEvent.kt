package dev.shog.buta.events

import dev.shog.buta.DEV
import dev.shog.buta.commands.CommandHandler
import dev.shog.buta.api.UserThreadHandler
import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.api.factory.UserFactory
import dev.shog.buta.api.obj.Category
import dev.shog.buta.events.obj.Event
import dev.shog.buta.handle.SwearFilter
import dev.shog.buta.util.orElse
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.kotlin.extra.bool.not
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * A message event.
 * It's also a coroutine scope, allowing for a thread for each new message.
 */
object MessageEvent : Event {
    override fun invoke(event: discord4j.core.event.domain.Event): Mono<*> {
        require(event is MessageCreateEvent)

        if (!event.message.author.isPresent || !event.guildId.isPresent)
            return Mono.empty<Void>()

        val obj = event.message.author.get().id.asLong()

        return UserFactory.getOrCreate(obj).toMono()
                .map { GuildFactory.getOrCreate(event.guildId.get().asLong()) }
                .flatMap { g ->
                    val content = event.message.content
                            .orElse("")
                            .toMono()

                    content
                            .filter { event.message.author.isPresent && !event.message.author.get().isBot }
                            .filterWhen { msg -> !SwearFilter.hasSwears(g, msg, event) }
                            .filter { data -> data.startsWith(g.prefix) }
                            .map { data -> data.removePrefix(g.prefix) }
                            .map { data -> data.split(" ") }
                            .filter { split -> split.isNotEmpty() }
                            .flatMap { con ->
                                Flux.fromIterable(CommandHandler.COMMANDS)
                                        .filter { en ->
                                            con[0].startsWith(en.container.name.toLowerCase(), true)
                                                    || en.container.aliases.contains(con[0])
                                        }
                                        .filterWhen { en -> en.cfg.permable.check(event) }
                                        .singleOrEmpty()
                                        .filter { entry ->
                                            UserThreadHandler.can(event.message.author.get(), entry.cfg.name)
                                        }
                                        .flatMap { entry ->
                                            val author = event.message.author.get()
                                            if (
                                                    Category.valueOf(entry.container.category.toUpperCase()) == Category.DEVELOPER &&
                                                    !DEV.contains(author.id.asLong()))
                                                event
                                                        .sendMessage("You must be a developer")
                                            else con.toMutableList()
                                                    .toMono()
                                                    .doOnNext { l -> l.removeAt(0) }
                                                    .flatMap { msg -> entry.invoke(event, msg) }
                                                    .doFinally { UserThreadHandler.finish(author, entry.container.name) }

                                        }
                            }
                }

    }
}