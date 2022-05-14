package dev.ajkneisl.home.printer

import dev.ajkneisl.printerlib.*
import java.util.*
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

/** Handles sending prints. */
object PrintHandler {
    private val MONGO_CLIENT =
        KMongo.createClient(
            "mongodb+srv://printerController:${System.getenv("MONGO_PW")}@ajknpr.hscnn.mongodb.net/myFirstDatabase?retryWrites=true&w=majority"
        )

    /** Print [lines] */
    fun print(vararg lines: PrintLine) {
        MONGO_CLIENT
            .getDatabase("printer")
            .getCollection<PrinterData>()
            .insertOne(
                PrintRequest(
                    Print(UUID.randomUUID().toString(), System.currentTimeMillis(), listOf(*lines))
                )
            )
    }

    /** A larger amount of [prints]. */
    fun batchPrint(prints: List<Print>) {
        MONGO_CLIENT
            .getDatabase("printer")
            .getCollection<PrinterData>()
            .insertOne(LargePrintRequest(prints))
    }
}
