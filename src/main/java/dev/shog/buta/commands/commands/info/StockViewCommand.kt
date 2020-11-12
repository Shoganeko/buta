package dev.shog.buta.commands.commands.info

import com.mitchtalmadge.asciidata.graph.ASCIIGraph
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.handle.StockHandler

import dev.shog.buta.util.sendMessage
import java.text.DecimalFormat
import java.util.*

val STOCK_VIEW_COMMAND = Command(CommandConfig(
        name = "stockview",
        category = Category.INFO,
        description = "View stocks.",
        help = hashMapOf(
                "stockview {symbol}" to "View stocks.",
                "stockview {symbol} {high/low/volume/close}" to "View specific data about a stock.",
                "stockview {symbol} {high/low/volume/close} {weekly/daily}" to "View specific data from a specific timestamp about stock."
        ),
        aliases = listOf("stv")
)) {
    val format = DecimalFormat("0.00")

    when (args.size) {
        // symbol
        1 -> {
            val symbol = args[0].toUpperCase()

            val stock = StockHandler.getStock(symbol, StockHandler.Time.WEEKLY, StockHandler.Type.CLOSE)

            val avg = stock.data.sum() / stock.data.size.toDouble()

            val size = format.format(avg).length

            val plot = ASCIIGraph
                    .fromSeries(stock.data.reversedArray())
                    .withTickFormat(DecimalFormat("0.00"))
                    .withNumRows(10)
                    .withTickWidth(size)
                    .plot()

            sendMessage("```Name: ${stock.symbol}\nType: close\nWeek High: ${Collections.max(stock.data.toList())}\nWeek Low: ${Collections.min(stock.data.toList())}\n\n${plot}```")
            return@Command
        }

        // symbol and type
        2 -> {
            val symbol = args[0]
            val type = args[1]

            val parsedType: StockHandler.Type = try {
                StockHandler.Type.values()
                        .single { ty -> ty.toString().equals(type, true) }
            } catch (ex: Exception) {
                sendMessage("Please choose between `high`, `low`, `volume` or `close`!")
                return@Command
            }

            val stock = StockHandler.getStock(symbol, StockHandler.Time.WEEKLY, parsedType)

            val avg = stock.data.sum() / stock.data.size.toDouble()

            val size = format.format(avg).length

            val plot = ASCIIGraph
                    .fromSeries(stock.data.reversedArray())
                    .withTickFormat(DecimalFormat("0.00"))
                    .withNumRows(10)
                    .withTickWidth(size)
                    .plot()

            sendMessage("```Name: ${stock.symbol}\nType: ${type.toLowerCase()}\nWeek High: ${Collections.max(stock.data.toList())}\nWeek Low: ${Collections.min(stock.data.toList())}\n\n${plot}```")
            return@Command
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
                sendMessage("Please choose between `high`, `low`, `volume` or `close`!")
                return@Command
            }

            val parsedTime: StockHandler.Time = try {
                StockHandler.Time.values()
                        .single { ty -> ty.toString().equals(time, true) }
            } catch (ex: Exception) {
                sendMessage("Please choose between `weekly` or `daily`!")
                return@Command
            }

            val stock = StockHandler.getStock(symbol, parsedTime, parsedType)

            val avg = stock.data.sum() / stock.data.size.toDouble()

            val size = format.format(avg).length

            val plot = ASCIIGraph
                    .fromSeries(stock.data.reversedArray())
                    .withTickFormat(DecimalFormat("0.00"))
                    .withNumRows(10)
                    .withTickWidth(size)
                    .plot()

            val message = when (parsedTime) {
                StockHandler.Time.WEEKLY -> "```Name: ${stock.symbol}\nType: ${type.toLowerCase()}\nWeek High: ${Collections.max(stock.data.toList())}\nWeek Low: ${Collections.min(stock.data.toList())}\n\n${plot}```"
                StockHandler.Time.DAILY -> "```Name: ${stock.symbol}\nType: ${type.toLowerCase()}\nDay High: ${Collections.max(stock.data.toList())}\nDay Low: ${Collections.min(stock.data.toList())}\n\n${plot}```"
            }

            sendMessage(message)
            return@Command
        }
    }

    sendMessage("Invalid arguments!")
}