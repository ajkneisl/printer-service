package dev.ajkneisl.home.printer.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Configures JSON serialization. */
fun Application.configureSerialization() {
    install(ContentNegotiation) { json() }
}
