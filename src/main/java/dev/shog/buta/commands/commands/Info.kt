package dev.shog.buta.commands.commands

import com.mitchtalmadge.asciidata.graph.ASCIIGraph

import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.ICommand.Companion.COMMANDS
import dev.shog.buta.handle.StockHandler
import dev.shog.buta.util.*
import discord4j.core.`object`.util.Image
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

/**
 * Initialize info commands
 */
fun initInfo() = Unit

/**
 * Help
 */
val HELP = Command("help", Categories.INFO) { e, args, lang ->
    if (args.size >= 1) {
        val command = args[0]

        return@Command Flux.fromIterable(COMMANDS)
                .filter { en -> command.startsWith(en.data.commandName.toLowerCase(), true) }
                .filterWhen { en -> en.permable.check(e) }
                .collectList()
                .flatMap { list ->
                    if (list.isNotEmpty()) {
                        list[0].invokeHelp(e)
                    } else e.sendMessage(lang.getString("inv-cmd"))
                }
                .then()
    }

    val helpCommand = Categories.values().toFlux()
            .flatMap { cat ->
                COMMANDS.toFlux()
                        .filter { cmd -> cmd.category == cat }
                        .filterWhen { cmd -> cmd.permable.hasPermission(e.message.author.get()) }
                        .map { cmd -> lang.getString("command").form(cmd.data.commandName) }
                        .collectList()
                        .filter { list -> list.isNotEmpty() }
                        .map { list ->
                            lang.getString("cat-pair").form(
                                    cat.name.capitalize(),
                                    list
                                            .stream()
                                            .map { str -> str.toLowerCase() }
                                            .collect(Collectors.joining())
                            )
                        }
                        .map { msg -> msg.substring(0, msg.length - 2) }
            }
            .collectList()
            .map { list -> list.stream().collect(Collectors.joining("\n\n")) }

    return@Command e.message.channel
            .zipWith(helpCommand)
            .flatMap { zip ->
                val ch = zip.t1

                ch.createEmbed { spec ->
                    lang.getJSONObject("help-embed").applyEmbed(spec, e.message.author.get(),
                            hashMapOf("desc" to arrayListOf(zip.t2)),
                            hashMapOf()
                    )
                }
            }
            .then()
}.build().add()

/**
 * Ping
 */
val PING = Command("ping", Categories.INFO) { e, _, lang ->
    e.sendMessage(lang.getString(lang.keySet().random()).form("12"))
            .then()
}.build().add()

/**
 * About Buta
 */
val ABOUT = Command("about", Categories.INFO) { e, _, lang ->
    e.sendMessage(lang.getString("default"))
            .then()
}.build().add()

/** The guild date formatter. */
private val FORMATTER = SimpleDateFormat("MM/dd/yyyy")

/**
 * Stock Viewer
 */
val STOCK_VIEW = Command("stockview", Categories.INFO, isPmAvailable = false) { e, args, lang ->
    val format = DecimalFormat("0.00")

    when (args.size) {
        // symbol
        1 -> {
            val symbol = args[0].toUpperCase()

            return@Command StockHandler.getStock(symbol, StockHandler.Time.WEEKLY, StockHandler.Type.CLOSE)
                    .doOnNext { stock -> println(stock.data.toList()) }
                    .flatMap { stock ->
                        val avg = stock.data.sum() / stock.data.size.toDouble()

                        val size = format.format(avg).length

                        val plot = ASCIIGraph
                                .fromSeries(stock.data.reversedArray())
                                .withTickFormat(DecimalFormat("0.00"))
                                .withNumRows(10)
                                .withTickWidth(size)
                                .plot()

                        e.sendMessage(lang.getString("success-weekly").form(
                                stock.symbol,
                                "close",
                                Collections.max(stock.data.toList()),
                                Collections.min(stock.data.toList()),
                                plot
                        ))
                    }
        }

        // symbol and type
        2 -> {
            val symbol = args[0]
            val type = args[1]

            val parsedType: StockHandler.Type = try {
                StockHandler.Type.values()
                        .single { ty -> ty.toString().equals(type, true) }
            } catch (ex: Exception) {
                return@Command e.sendMessage(lang.getString("invalid-type"))
            }

            return@Command StockHandler.getStock(symbol, StockHandler.Time.WEEKLY, parsedType)
                    .doOnNext { stock -> println(stock.data.toList()) }
                    .flatMap { stock ->
                        val avg = stock.data.sum() / stock.data.size.toDouble()

                        val size = format.format(avg).length

                        val plot = ASCIIGraph
                                .fromSeries(stock.data.reversedArray())
                                .withTickFormat(DecimalFormat("0.00"))
                                .withNumRows(10)
                                .withTickWidth(size)
                                .plot()

                        e.sendMessage(lang.getString("success-weekly").form(
                                stock.symbol,
                                type.toLowerCase(),
                                Collections.max(stock.data.toList()),
                                Collections.min(stock.data.toList()),
                                plot
                        ))
                    }
        }

        // symbol, type and time
        3 -> {
            val symbol = args[0]
            val type = args[1]
            val time = args[2]

            val parsedType: StockHandler.Type = try {
                StockHandler.Type.values()
                        .single { ty -> ty.toString().equals(type, true) }
            } catch (ex: Exception) {
                return@Command e.sendMessage(lang.getString("invalid-type"))
            }

            val parsedTime: StockHandler.Time = try {
                StockHandler.Time.values()
                        .single { ty -> ty.toString().equals(time, true) }
            } catch (ex: Exception) {
                return@Command e.sendMessage(lang.getString("invalid-time"))
            }

            return@Command StockHandler.getStock(symbol, parsedTime, parsedType)
                    .doOnNext { stock -> println(stock.data.toList()) }
                    .flatMap { stock ->
                        val avg = stock.data.sum() / stock.data.size.toDouble()

                        val size = format.format(avg).length

                        val plot = ASCIIGraph
                                .fromSeries(stock.data.reversedArray())
                                .withTickFormat(DecimalFormat("0.00"))
                                .withNumRows(10)
                                .withTickWidth(size)
                                .plot()

                        val langKey = when (parsedTime) {
                            StockHandler.Time.WEEKLY -> "success-weekly"
                            StockHandler.Time.DAILY -> "success-daily"
                        }

                        e.sendMessage(lang.getString(langKey).form(
                                stock.symbol,
                                type.toLowerCase(),
                                Collections.max(stock.data.toList()),
                                Collections.min(stock.data.toList()),
                                plot
                        ))
                    }
        }
    }

    return@Command e.sendMessage("error.invalid_arguments")
}.build().add()

/**
 * About Guild
 */
val GUILD = Command("guild", Categories.INFO, isPmAvailable = false) { e, args, lang ->
    return@Command when {
        args.size == 0 ->
            e.message.channel
                    .zipWith(e.message.guild)
                    .flatMap { zip ->
                        val ch = zip.t1
                        val g = zip.t2

                        ch.createEmbed { spec ->
                            lang.getJSONObject("local-guild").applyEmbed(
                                    spec,
                                    e.message.author.get(),
                                    hashMapOf(
                                            "image" to g.getIconUrl(Image.Format.JPEG).orElse("").ar(),
                                            "title" to g.name.ar()
                                    ),
                                    hashMapOf(
                                            "user-count" to FieldReplacement(null, g.memberCount.asInt.toString().ar()),
                                            "date" to FieldReplacement(null, FORMATTER.format(Date.from(g.id.timestamp)).ar())
                                    )
                            )
                        }
                    }
                    .then()

        args[0].equals("global", true) ->
            e.message.channel
                    .flatMap { ch ->
                        ch.client.guilds
                                .collectList()
                                .map { list -> list.size }
                                .zipWith(
                                        ch.client.users
                                                .collectList()
                                                .map { list -> list.size }
                                )
                                .flatMap { list ->
                                    ch.createEmbed { spec ->
                                        lang.getJSONObject("local-guild").applyEmbed(
                                                spec,
                                                e.message.author.get(),
                                                hashMapOf(
                                                        "image" to "https://cdn.frankerfacez.com/emoticon/256055/4".ar(),
                                                        "desc" to arrayListOf(list.t1.toString(), list.t2.toString())
                                                ),
                                                hashMapOf()
                                        )
                                    }
                                }
                    }
                    .then()

        else ->
            e.sendMessage("error.invalid_arguments").then()
    }
}.build().add()