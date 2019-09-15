package dev.shog.buta.events

import dev.shog.buta.commands.UserThreadHandler
import dev.shog.buta.commands.api.guild.GuildFactory
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.events.obj.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * A message event.
 * It's also a coroutine scope, allowing for a thread for each new message.
 */
object MessageEvent : Event(), CoroutineScope by CoroutineScope(Dispatchers.Unconfined) {
    override fun invoke(event: discord4j.core.event.domain.Event) {
        require(event is MessageCreateEvent)

        launch {
            if (event.message.author.isPresent && !event.message.author.get().isBot && UserThreadHandler.can(event.message.author.get())) {
                GuildFactory.getGuild(event.guildId.get()).subscribe { g ->
                    Mono.justOrEmpty(event.message.content)
                            .flatMap { content ->
                                Flux.fromIterable(Command.COMMANDS)
                                        .filter { entry ->
                                            content.startsWith("${g.getString("prefix")
                                                    ?: "b!"}${entry.meta.commandName.toLowerCase()}", true)
                                        }
                                        .flatMap { entry ->
                                            if (!entry.meta.isPmAvailable && !event.guildId.isPresent)
                                                event.message.channel
                                                        .flatMap { ch -> ch.createMessage("You can't use this here!") }
                                            else Mono.justOrEmpty(event.message.content)
                                                    .flatMapMany { msg -> Flux.just(msg.split(" ").toMutableList()) }
                                                    .doOnNext { l -> l.removeAt(0) }
                                                    .flatMap { msg -> Mono.just(launch { entry.invoke(event, msg) }) }
                                        }
                                        .next()
                            }
                            .doOnSubscribe { UserThreadHandler.finish(event.message.author.get()) }
                            .subscribe()
                }
            }
        }
    }
}