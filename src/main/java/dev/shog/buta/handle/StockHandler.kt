package dev.shog.buta.handle

import dev.shog.buta.APP
import kong.unirest.Unirest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * This is one of the only APIs I could find that fit what I needed.
 * The way it outputs things is stupid and I hate it. This is why some things are scuffed.
 * Like, why are there spaces in keys? Why is it not consistent throughout different "?function="s?.
 */
object StockHandler {
    private val URL = "https://www.alphavantage.co/query?function={func}&symbol={sym}&apikey=${APP.getConfigObject<ButaConfig>().stocks}"

    /**
     * A stock's data.
     *
     * @param symbol The stock symbol. (EX: MSFT)
     * @param timezone The timezone where it was updated from.
     * @param data The data from the check
     * @param stock The time to get the stock from.
     * @param type The type of value retrieved.
     */
    class StockData constructor(val symbol: String, private val timezone: String, val data: DoubleArray, private val stock: Time, val type: Type)

    /**
     * The timespan to show.
     *
     * @param func The function that is included in the URL.
     * @param key The key to retrieve the data.
     */
    enum class Time(val func: String, val key: String) {
        /**
         * Show daily data over the week.
         */
        WEEKLY("TIME_SERIES_DAILY", "Time Series (Daily)"),

        /**
         * Show a single day's data with 15 minute intervals.
         */
        DAILY("TIME_SERIES_INTRADAY&interval=15min", "Time Series (15min)")
    }

    /**
     * The type of data to retrieve.
     *
     * @param key The key to get the value in the god awful JSON response.
     */
    enum class Type(val key: String) {
        /**
         * See the high of the segment
         */
        HIGH("2. high"),

        /**
         * THe low of the segment
         */
        LOW("3. low"),

        /**
         * The volume of the segment
         */
        VOLUME("5. volume"),

        /**
         * The close of the segment
         */
        CLOSE("4. close")
    }

    /**
     * Get [sym]'s stock.
     *
     * @param time The timespan to receive the stock from.
     * @param type The type of data to retrieve.
     */
    fun getStock(sym: String, time: Time, type: Type): Mono<StockData> {
        val builtUrl = URL
                .replace("{sym}", sym)
                .replace("{func}", time.func)

        return Unirest.get(builtUrl)
                .asJsonAsync()
                .toMono()
                .doOnNext { js -> println(js.body) }
                .filter { js -> !js.body.`object`.has("Error Message") }
                .map { js ->
                    val obj = js.body.`object`

                    val tz = obj.getJSONObject("Meta Data")

                    val tsd = obj.getJSONObject(time.key)
                    val values = mutableListOf<Double>()

                    val timeZone = when (time) {
                        Time.WEEKLY -> {
                            val amount = if (8 > tsd.keySet().size) tsd.keySet().size else 8

                            val keys = tsd.keys()
                            while (amount > values.size) {
                                val day = tsd.getJSONObject(keys.next())
                                val value = day.getDouble(type.key)

                                values.add(value)
                            }

                            tz.getString("5. Time Zone")
                        }

                        Time.DAILY -> {
                            val firstKey = tsd.keys().next().split(" ")[0]

                            tsd.keys().asSequence()
                                    .filter { key -> key.startsWith(firstKey) }
                                    .map { key -> tsd.getJSONObject(key) }
                                    .map { newObj -> newObj.getDouble(type.key) }
                                    .forEach { value -> values.add(value) }

                            tz.getString("6. Time Zone")
                        }
                    }

                    StockData(sym, timeZone, values.toDoubleArray(), time, type)
                }
                .cache()
    }
}