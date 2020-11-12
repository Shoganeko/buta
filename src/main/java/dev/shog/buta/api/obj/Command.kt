package dev.shog.buta.api.obj

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.api.obj.msg.MessageHandler
import dev.shog.buta.util.addFooter

/**
 * A command.
 *
 * @param cfg The config
 */
class Command(val cfg: CommandConfig, val cmd: suspend CommandContext.() -> Unit) : ICommand {
    val container = MessageHandler.MessageContainer(cfg.name)

    override suspend fun invoke(e: MessageCreateEvent, args: MutableList<String>) =
            cmd.invoke(CommandContext(container, e, args))

    override suspend fun help(e: MessageCreateEvent) {
        e.message.channel.createEmbed {
            val guild = GuildFactory.getOrCreate(e.guildId?.longValue ?: return)

            addFooter(e)

            title = cfg.name
            description = container.desc

            val obj = MessageHandler.data
                    .getJSONObject(cfg.name)
                    .getJSONObject("help")

            obj.keys()
                    .forEach { key ->
                        field {
                            name = "${guild.prefix}${key}"
                            value = obj.getString(key)
                        }
                    }
        }
    }
}