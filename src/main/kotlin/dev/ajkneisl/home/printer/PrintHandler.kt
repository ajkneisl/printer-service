package dev.ajkneisl.home.printer

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.event.ServerHeartbeatFailedEvent
import com.mongodb.event.ServerMonitorListener
import dev.ajkneisl.printerlib.*
import java.util.*
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Handles sending prints. */
object PrintHandler {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)

    /** Ensures Mongo doesn't get disconnected. */
    object Mongo {
        lateinit var client: MongoClient

        private object Listener : ServerMonitorListener {
            override fun serverHeartbeatFailed(event: ServerHeartbeatFailedEvent?) {
                LOGGER.error("MongoDB failed a heartbeat, creating a new client.")

                createClient()
            }
        }

        /** Get the MongoDB connection string. */
        private fun getConnectionString() =
            "mongodb+srv://printServer:${getSecret("API_KEY_MONGO")}@ajknpr.hscnn.mongodb.net/myFirstDatabase?retryWrites=true&w=majority"

        /** Instantiate a new MongoDB client. Applies [Listener]. */
        private fun createClient() {
            client =
                KMongo.createClient(
                    MongoClientSettings.builder()
                        .applyConnectionString(ConnectionString(getConnectionString()))
                        .applyToServerSettings { it.addServerMonitorListener(Listener) }
                        .build()
                )
        }
        init {
            createClient()
        }
    }

    /** Print [lines] */
    fun print(vararg lines: PrintLine) {
        val print: PrinterData =
            PrintRequest(
                Print(UUID.randomUUID().toString(), System.currentTimeMillis(), listOf(*lines))
            )

        print(print)
    }

    /** Print [req] */
    private fun print(req: PrinterData) {
        if (!System.getenv().getOrDefault("TEST_MODE", "false").toBoolean())
            Mongo.client.getDatabase("printer").getCollection<PrinterData>("queue").insertOne(req)
    }

    /** A larger amount of [prints]. */
    fun batchPrint(prints: List<Print>) {
        val print: PrinterData = LargePrintRequest(prints)

        if (!System.getenv().getOrDefault("TEST_MODE", "false").toBoolean())
            Mongo.client.getDatabase("printer").getCollection<PrinterData>("queue").insertOne(print)
    }
}
