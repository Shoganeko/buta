package dev.shog.buta.events

import dev.shog.buta.commands.UserThreadHandler
import dev.shog.buta.commands.api.GuildFactory
import dev.shog.buta.commands.obj.Command
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
            GuildFactory.get(event.guildId.get().asLong())
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
                                .flatMap { con ->
                                    Flux.fromIterable(Command.COMMANDS)
                                            .filter { en ->
                                                con?.startsWith(en.data.commandName.toLowerCase(), true) == true
                                            }
                                            .filterWhen { en -> en.permable.check(event) }
                                            .collectList()
                                            .flatMap { commandList ->
                                                if (commandList.isNotEmpty()) {
                                                    val entry = commandList[0]!!

                                                    Mono.justOrEmpty(con)
                                                            .flatMapMany { msg ->
                                                                Flux.just(msg?.split(" ")?.toMutableList()
                                                                        ?: mutableListOf("cmdstring"))
                                                            }
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