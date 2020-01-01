package dev.shog.buta.events

import dev.shog.buta.commands.UserThreadHandler
import dev.shog.buta.commands.api.GuildFactory
import dev.shog.buta.commands.api.UserFactory
import dev.shog.buta.commands.obj.ICommand
import dev.shog.buta.events.obj.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * A message event.
 * It's also a coroutine scope, allowing for a thread for each new message.
 */
object MessageEvent : Event, CoroutineScope by CoroutineScope(Dispatchers.Unconfined) {
    override fun invoke(event: discord4j.core.event.domain.Event): Mono<Void> {
        require(event is MessageCreateEvent)

        return if (
                event.message.author.isPresent
                && !event.message.author.get().isBot
                && UserThreadHandler.can(event.message.author.get())
        ) {
            UserFactory.getOrCreate(event.message.author.get().id.asLong())
                    .then(GuildFactory.get(event.guildId.get().asLong()))
                    .flatMap { g ->
                        val content = event.message.content
                                .orElse("")
                                .toMono()

                        content
                                .flatMap { data ->
                                    if (data.startsWith(g.prefix)) {
                                        data.removePrefix(g.prefix)
                                                .toMono()
                                    } else Mono.empty()
                                }
                                .map { data -> data.split(" ") }
                                .filter { split -> split.isNotEmpty() }
                                .flatMap { con ->
                                    Flux.fromIterable(ICommand.COMMANDS)
                                            .filter { en ->
                                                con[0].startsWith(en.data.commandName.toLowerCase(), true)
                                                        || en.data.alias.contains(con[0])
                                            }
                                            .filterWhen { en -> en.permable.check(event) }
                                            .collectList()
                                            .flatMap { commandList ->
                                                if (commandList.isNotEmpty()) {
                                                    val entry = commandList[0]!!

                                                    con.toMutableList()
                                                            .toMono()
                                                            .doOnNext { l -> l.removeAt(0) }
                                                            .flatMap { msg -> entry.invoke(event, msg) }
                                                            .then()
                                                } else Mono.empty()
                                            }
                                }
                    }
                    .doFinally { UserThreadHandler.finish(event.message.author.get()) }
        } else Mono.empty()
    }
}