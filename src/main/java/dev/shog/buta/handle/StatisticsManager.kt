package dev.shog.buta.handle

import dev.shog.buta.APP
import java.util.concurrent.ConcurrentHashMap

/**
 * Manage statistics
 */
object StatisticsManager {
    private var statistics = ConcurrentHashMap<String, Any>()
    fun dump() = statistics.toString()

    init {
        statistics = APP.getCache().getObject<ConcurrentHashMap<String, Any>>("stats")?.getValue()
                ?: ConcurrentHashMap()
    }

    /**
     * Save stats to disk
     */
    fun save() {
        APP.getCache().createObject("stats", statistics)
    }

    /**
     * Set a statistic using it's [key].
     */
    fun setStatistic(key: String, value: Any) {
        statistics[key] = value
    }

    /**
     * Get a statistic by it's [key].
     */
    fun getStatistic(key: String) = statistics[key]

    /**
     * If [key] is a number, increase it by one.
     */
    fun increaseStatistic(key: String) {
        val stat = statistics[key] ?: 0

        if (stat is Number)
            statistics[key] = stat.toInt() + 1
    }

    /**
     * If [key] is a number, decrease it by one.
     */
    fun decreaseStatistic(key: String) {
        val stat = statistics[key] ?: 0

        if (stat is Number)
            statistics[key] = stat.toInt() - 1
    }
}