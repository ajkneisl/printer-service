package dev.ajkneisl.home.printer

import dev.ajkneisl.home.printer.mongo.Mongo.CLIENT
import dev.ajkneisl.printer.obj.Print
import dev.ajkneisl.printer.obj.PrintLine
import org.litote.kmongo.getCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object PrintHandler {
    private val PRINTER_LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Automatically create a [Print] object.
     */
    fun print(title: String, subtitle: String, qrCode: String? = null, vararg lines: PrintLine) {
        print(
            Print(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                title,
                subtitle,
                lines.toList(),
                qrCode
            )
        )
    }

    /**
     * Insert [print] into printqueue.
     */
    fun print(print: Print) {
        PRINTER_LOGGER.info("Printing: ${print.id} created at ${print.createdAt}")

        try {
            CLIENT.getDatabase("printer")
                .getCollection<Print>("printqueue")
                .insertOne(
                    print
                )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Conveniently define content and feed of a [PrintLine]
     */
    infix fun String.feed(feed: Int): PrintLine {
        return PrintLine(this, feed)
    }
}