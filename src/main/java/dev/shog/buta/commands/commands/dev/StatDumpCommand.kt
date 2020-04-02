package dev.shog.buta.commands.commands.dev

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.handle.StatisticsManager
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class StatDumpCommand : Command(CommandConfig(
        "statdump",
        "Statistics dump",
        Category.DEVELOPER,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        if (args.size == 1 && args[0].equals("save", true))
            return e.sendMessage(container, "saved")
                    .doOnNext { StatisticsManager.save() }

        return StatisticsManager.dump().toMono()
                .flatMap { dump -> e.sendMessage(container, "dump", dump) }
    }
}