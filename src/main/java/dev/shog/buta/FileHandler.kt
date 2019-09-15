package dev.shog.buta

import org.apache.commons.lang3.SystemUtils
import org.json.JSONObject
import java.io.File
import kotlin.system.exitProcess

/**
 * The file handler. Gets the configuration file.
 *
 * You cannot set variables through this object.
 */
object FileHandler {
    /**
     * The directory where the configuration file is stored.
     */
    private val BUTA_DIR = File(when {
        SystemUtils.IS_OS_WINDOWS_10 -> "${System.getenv("appdata")}\\buta"
        SystemUtils.IS_OS_LINUX -> "/etc/buta"
        else -> {
            LOGGER.error("Invalid OS! Please use Windows 10 or Linux (Ubuntu).")
            exitProcess(-1)
        }
    })

    /**
     * The configuration file.
     */
    private val CFG_FILE = File(BUTA_DIR.path + File.separator + "cfg.json")

    init {
        if (!BUTA_DIR.exists() && !BUTA_DIR.mkdir()) {
            LOGGER.error("There was an issue creating the Buta folder.")
            exitProcess(-1)
        }

        if (!CFG_FILE.exists()) {
            if (CFG_FILE.createNewFile()) {
                initCfg()
            } else {
                LOGGER.error("There was an issue creating the configuration file.")
                exitProcess(-1)
            }
        }
    }

    /**
     * If [CFG_FILE] is empty, initialize it.
     */
    private fun initCfg() {
        if (CFG_FILE.exists()) {
            val str = String(CFG_FILE.inputStream().readBytes())

            if (str.isBlank()) {
                val stream = CFG_FILE.outputStream().bufferedWriter()

                stream.write("{\"token\": \"\"}")

                stream.flush()
                stream.close()
            }
        } else {
            LOGGER.error("There was an issue getting the configuration file.")
            exitProcess(-1)
        }
    }

    /**
     * The JSON from the file.
     */
    private val variables = if (CFG_FILE.exists()) {
        val str = String(CFG_FILE.inputStream().readBytes())

        try {
            JSONObject(str)
        } catch (ex: Exception) {
            LOGGER.error("The configuration file is incorrectly formatted!")
            exitProcess(-1)
        }
    } else {
        LOGGER.error("There was an issue getting the configuration file.")
        exitProcess(-1)
    }

    /**
     * Gets a key from the variables.
     */
    fun get(key: String): Any? = variables.get(key)
}