package dev.shog.buta.commands.commands.info

import com.mitchtalmadge.asciidata.graph.ASCIIGraph
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.handle.StockHandler
import dev.shog.buta.util.sendGlobalMessage

import dev.shog.buta.util.sendMessage
import java.text.DecimalFormat
import java.util.*

val STOCK_VIEW_COMMAND = Command(CommandConfig("stockview")) {
    val format = DecimalFormat("0.00")

    when (args.size) {
        // symbol
        1 -> {
            val symbol = args[0].toUpperCase()

            return@Command StockHandler.getStock(symbol, StockHandler.Time.WEEKLY, StockHandler.Type.CLOSE)
                    .flatMap { stock ->
                        val avg = stock.data.sum() / stock.data.size.toDouble()

                        val size = format.format(avg).length

                        val plot = ASCIIGraph
                                .fromSeries(stock.data.reversedArray())
                                .withTickFormat(DecimalFormat("0.00"))
                                .withNumRows(10)
                                .withTickWidth(size)
                                .plot()

                        sendMessage(
                                "success-weekly",
                                stock.symbol,
                                "close",
                                Collections.max(stock.data.toList()),
                                Collections.min(stock.data.toList()),
                                plot
                        )
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
                return@Command sendMessage("invalid-type")
            }

            return@Command StockHandler.getStock(symbol, StockHandler.Time.WEEKLY, parsedType)
                    .flatMap { stock ->
                        val avg = stock.data.sum() / stock.data.size.toDouble()

                        val size = format.format(avg).length

                        val plot = ASCIIGraph
                                .fromSeries(stock.data.reversedArray())
                                .withTickFormat(DecimalFormat("0.00"))
                                .withNumRows(10)
                                .withTickWidth(size)
                                .plot()

                        sendMessage(
                                "success-weekly",
                                stock.symbol,
                                type.toLowerCase(),
                                Collections.max(stock.data.toList()),
                                Collections.min(stock.data.toList()),
                                plot
                        )
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
                return@Command sendMessage("invalid-type")
            }

            val parsedTime: StockHandler.Time = try {
                StockHandler.Time.values()
                        .single { ty -> ty.toString().equals(time, true) }
            } catch (ex: Exception) {
                return@Command sendMessage("invalid-time")
            }

            return@Command StockHandler.getStock(symbol, parsedTime, parsedType)
                    .doOnNext { stock -> (stock.data.toList()) }
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

                        sendMessage(
                                langKey,
                                stock.symbol,
                                type.toLowerCase(),
                                Collections.max(stock.data.toList()),
                                Collections.min(stock.data.toList()),
                                plot
                        )
                    }
        }
    }

    sendGlobalMessage("error.invalid-arguments")
}