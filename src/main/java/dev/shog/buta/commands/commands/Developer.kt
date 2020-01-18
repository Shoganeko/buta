package dev.shog.buta.commands.commands

import dev.shog.buta.commands.UserThreadHandler
import dev.shog.buta.commands.api.Api
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.events.PresenceHandler
import dev.shog.buta.handle.StatisticsManager
import dev.shog.buta.util.form
import dev.shog.buta.util.sendMessage
import dev.shog.lib.util.getOrNull
import reactor.kotlin.core.publisher.toMono
import java.util.stream.Collectors

/**
 * Manage presence on Discord.
 */
val PRESENCE = Command("presence", Categories.DEVELOPER) { e, args, lang ->
    when {
        args.size == 1 && args[0].equals("server", true) -> {
            e.sendMessage(lang.getString("server-side"))
                    .flatMap { Api.refreshPresences() }
                    .then()
        }

        args.size == 1 && args[0].equals("local", true) -> {
            e.sendMessage(lang.getString("client-side"))
                    .flatMap { PresenceHandler.updatePresences() }
                    .then()
        }

        args.size == 1 && args[0].equals("dump", true) -> {
            PresenceHandler.presences
                    .stream()
                    .map { pres -> "`$pres`" }
                    .collect(Collectors.joining(", "))
                    .toMono()
                    .flatMap { built -> e.sendMessage(lang.getString("dump").form(built)) }
                    .then()
        }

        else -> {
            e.sendMessage(lang.getString("update"))
                    .flatMap { PresenceHandler.update(e.client) }
                    .then()
        }
    }
}.build().add()

/**
 * Dump statistics.
 */
val STAT_DUMP = Command("statdump", Categories.DEVELOPER) { e, args, lang ->
    if (args.size == 1 && args[0].equals("save", true))
        return@Command e.sendMessage(lang.getString("saved"))
                .doOnNext { StatisticsManager.save() }
                .then()

    StatisticsManager.dump()
            .toMono()
            .flatMap { dump -> e.sendMessage(lang.getString("dump").form(dump)) }
            .then()
}.build().add()

/**
 * View current threads
 */
val THREAD_VIEW = Command("viewthreads", Categories.DEVELOPER) { e, args, lang ->
    when (args.getOrNull(0)) {
        "clear" -> e.sendMessage(lang.getString("cleared"))
                .doOnNext { UserThreadHandler.users.remove(e.message.author.get()) }
                .then()

        else ->
            e.sendMessage(lang.getString("response")
                    .form(UserThreadHandler.users.getOrNull(e.message.author.get())))
                    .then()
    }
}.build().add()
