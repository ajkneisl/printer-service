package dev.ajkneisl.home.printer

import dev.ajkneisl.printerlib.PrintDefaults
import dev.ajkneisl.printerlib.PrintText
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.configurePurchaseAlerts() {
    routing {
        post("/purchase") {
            val params = call.receiveParameters()

            PrintHandler.print(
                PrintText(PrintDefaults.TITLE, 0, params["subject"] ?: "No subject."),
                PrintText(PrintDefaults.SUB_TITLE, 0, params["from"] ?: "No sender."),
                PrintText(PrintDefaults.DEFAULT, 0, params["text"] ?: "")
            )
        }
    }
}
