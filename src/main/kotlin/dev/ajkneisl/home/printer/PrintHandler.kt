package dev.ajkneisl.home.printer

import dev.ajkneisl.printerlib.*
import kotlinx.serialization.encodeToString
import java.util.*
import kotlinx.serialization.json.Json
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.litote.kmongo.serialization.SerializationClassMappingTypeService

/** Handles sending prints. */
object PrintHandler {
    init {
        System.setProperty(
            "org.litote.mongo.mapping.service",
            SerializationClassMappingTypeService::class.qualifiedName!!
        )
    }

    private val MONGO_CLIENT =
        KMongo.createClient(
            "mongodb+srv://printerController:${System.getenv("API_KEY_MONGO")}@ajknpr.hscnn.mongodb.net/myFirstDatabase?retryWrites=true&w=majority"
        )

    /** Print [lines] */
    fun print(vararg lines: PrintLine) {
        val print: PrinterData =
            PrintRequest(
                Print(UUID.randomUUID().toString(), System.currentTimeMillis(), listOf(*lines))
            )

        MONGO_CLIENT.getDatabase("printer").getCollection<PrinterData>("queue").insertOne(print)
    }

    /** A larger amount of [prints]. */
    fun batchPrint(prints: List<Print>) {
        val print: PrinterData = LargePrintRequest(prints)

        MONGO_CLIENT.getDatabase("printer").getCollection<PrinterData>("queue").insertOne(print)
    }
}
