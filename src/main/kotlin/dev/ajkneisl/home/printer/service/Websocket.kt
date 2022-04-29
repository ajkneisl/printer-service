package dev.ajkneisl.home.printer.service

import dev.ajkneisl.printerlib.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/** The websocket logger. */
private val LOGGER = LoggerFactory.getLogger("WS")

/** A printer connection. */
private class PrinterConnection(val printerId: String, val connection: DefaultWebSocketSession)

/** Connections to the websocket. */
private val CONNECTIONS = Collections.synchronizedSet<PrinterConnection?>(LinkedHashSet())

/** Send a print to the printers. */
suspend fun sendPrint(print: Print) {
    CONNECTIONS.forEach { con ->
        LOGGER.info("Sending print to ${con.printerId}.")

        con.connection.send(Json.encodeToString(PrintRequest(print) as SocketMessage))
    }
}

/**
 * Send a group of prints.
 */
suspend fun sendPrints(prints: List<Print>) {
    CONNECTIONS.forEach { con ->
        LOGGER.info("Sending group print to ${con.printerId}.")

        con.connection.send(Json.encodeToString(LargePrintRequest(prints) as SocketMessage))
    }
}

/** Register the `/watch` websocket. */
fun Application.registerWebsocket() {

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/watch") {
            LOGGER.info("New Connection.")
            var connection: PrinterConnection? = null

            launch {
                while (connection == null) {
                    delay(1000)
                    sendSerialized(RequestAuthentication() as SocketMessage)
                }
            }

            try {
                for (frame in incoming) {
                    LOGGER.debug(
                        "Incoming: ${(this.incoming.receive() as? Frame.Text)?.readText()}"
                    )

                    val incoming: SocketMessage =
                        try {
                            receiveDeserialized()
                        } catch (ex: WebsocketDeserializeException) {
                            sendSerialized(ErrorResponse("Invalid serialization.") as SocketMessage)
                            continue
                        }

                    when (incoming) {
                        is SocketRequest -> {
                            when (incoming) {
                                is Authenticate -> {
                                    if (!Authorization.authorize(incoming.id, incoming.token)) {
                                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid Authorization"))
                                        continue
                                    }

                                    LOGGER.info("${incoming.id}: Successfully authorized.")

                                    val con = PrinterConnection(incoming.id, this)

                                    connection = con
                                    CONNECTIONS.add(con)

                                    sendSerialized(
                                        SuccessResponse("Successfully authenticated.") as
                                            SocketMessage
                                    )
                                }
                                else -> {
                                    LOGGER.error("${connection?.printerId}: Unknown request.")
                                }
                            }
                        }
                        is SocketResponse -> {
                            when (incoming) {
                                is SuccessResponse ->
                                    LOGGER.info("${connection?.printerId}: ${incoming.message}")
                                is ErrorResponse ->
                                    LOGGER.error("${connection?.printerId}: ${incoming.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                LOGGER.info("Disconnected.")
                CONNECTIONS -= connection
            }
        }
    }
}
