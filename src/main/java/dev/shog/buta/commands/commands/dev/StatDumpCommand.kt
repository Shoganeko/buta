package dev.shog.buta.commands.commands.dev

import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.handle.StatisticsManager
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

val STAT_DUMP_COMMAND = Command(CommandConfig("statdump")) {
    if (args.size == 1 && args[0].equals("save", true))
        return@Command sendMessage("saved")
                .doOnNext { StatisticsManager.save() }

    return@Command StatisticsManager.dump().toMono()
            .flatMap { dump -> sendMessage("dump", dump) }
}