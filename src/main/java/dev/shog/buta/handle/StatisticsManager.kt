package dev.shog.buta.handle

import dev.shog.buta.FileHandler
import dev.shog.buta.LOGGER
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Manage statistics
 */
object StatisticsManager {
    var statistics = ConcurrentHashMap<String, Any>()
    fun dump() = statistics.toString()

    init {
        val file = File(FileHandler.BUTA_DIR.path + "${File.separator}statCache")

        if (!file.exists())
            file.createNewFile()
        else {
            try {
                val ois = ObjectInputStream(file.inputStream())

                statistics = ois.readObject() as? ConcurrentHashMap<String, Any>
                        ?: ConcurrentHashMap()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Save stats to disk
     */
    fun save() {
        LOGGER.info("Saving statistics to disk...")

        val file = File(FileHandler.BUTA_DIR.path + "${File.separator}statCache")

        if (!file.exists())
            file.createNewFile()

        val stats = statistics
        val oos = ObjectOutputStream(FileOutputStream(file))

        oos.writeObject(stats)

        LOGGER.info("Statistics have been saved to disk!")
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