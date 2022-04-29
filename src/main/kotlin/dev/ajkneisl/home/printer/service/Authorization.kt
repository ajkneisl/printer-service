package dev.ajkneisl.home.printer.service

import java.io.File
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Authorization for the websocket. */
object Authorization {
    private val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    private val AUTH = hashMapOf<String, String>()

    init {
        loadFromFile(File("/etc/ajkn/.auth"))
    }

    /** Load authorization properties from [file]. */
    private fun loadFromFile(file: File) {
        LOGGER.info("Loading authorization properties for: ${file.path}")
        val fileContents = String(file.readBytes())
        val obj = JSONObject(fileContents)

        obj.keys().forEach { key ->
            LOGGER.debug("Found authorization for: $key")
            AUTH[key] = obj.getString(key)
        }
    }

    /** Authorize [id] with [token]. */
    fun authorize(id: String, token: String): Boolean {
        return AUTH.containsKey(id.lowercase()) && AUTH[id.lowercase()] == token
    }
}
