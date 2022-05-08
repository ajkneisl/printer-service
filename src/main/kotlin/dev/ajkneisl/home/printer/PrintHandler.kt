package dev.ajkneisl.home.printer

import dev.ajkneisl.home.printer.service.sendPrint
import dev.ajkneisl.printerlib.Print
import dev.ajkneisl.printerlib.PrintLine
import java.util.*

/** Handles sending prints. */
object PrintHandler {
    /** Print [lines] */
    suspend fun print(vararg lines: PrintLine) {
        sendPrint(Print(UUID.randomUUID().toString(), System.currentTimeMillis(), listOf(*lines)))
    }
}
