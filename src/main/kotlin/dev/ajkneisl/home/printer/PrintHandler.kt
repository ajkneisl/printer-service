package dev.ajkneisl.home.printer

import dev.ajkneisl.home.printer.service.sendPrint
import dev.ajkneisl.printerlib.Print
import dev.ajkneisl.printerlib.PrintLine
import java.util.*

object PrintHandler {
    /** Print [lines] */
    suspend fun print(vararg lines: PrintLine) {
        sendPrint(Print(UUID.randomUUID().toString(), System.currentTimeMillis(), listOf(*lines)))
    }

    /**
     * Print all of [prints].
     */
    suspend fun groupPrint(prints: List<Print>) {
        prints.forEach { print ->
            sendPrint(print)
        }
    }
}
