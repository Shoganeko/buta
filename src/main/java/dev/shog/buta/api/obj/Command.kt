package dev.shog.buta.api.obj

import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.api.obj.msg.MessageHandler
import dev.shog.buta.util.update
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/**
 * A command.
 *
 * @param cfg The config
 */
class Command(val cfg: CommandConfig, val cmd: CommandContext.() -> Mono<*>) : ICommand {
    val container = MessageHandler.MessageContainer(cfg.name)

    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> =
            cmd.invoke(CommandContext(container, e, args))

    override fun help(e: MessageCreateEvent): Mono<*> =
            e.message.guild
                    .map { g -> g.id.asLong() }
                    .zipWith(e.message.channel)
                    .flatMap { zip ->
                        val ch = zip.t2
                        val g = GuildFactory.getOrCreate(zip.t1)

                        ch.createEmbed { embed ->
                            embed.update(e.message.author.get())

                            embed.setTitle(cfg.name)
                            embed.setDescription(container.desc)

                            val obj = MessageHandler.data
                                    .getJSONObject(cfg.name)
                                    .getJSONObject("help")

                            obj.keys().forEach { key ->
                                val value = obj.getString(key)

                                embed.addField("${g.prefix}${key}", value, false)
                            }
                        }
                    }
                    .then()
}