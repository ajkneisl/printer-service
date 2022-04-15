@file:OptIn(KtorExperimentalLocationsAPI::class)

package dev.ajkneisl.home.printer.plugins

import dev.ajkneisl.home.printer.authorize
import dev.ajkneisl.home.printer.error.ServerError
import dev.ajkneisl.home.printer.routines.goodMorning
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(AutoHeadResponse)
    install(DoubleReceive)
    install(Locations)

    install(StatusPages) {
        exception<ServerError>() { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("response" to cause.message))
        }

        exception { call: ApplicationCall, cause: Exception ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, mapOf("response" to "There was an issue on our end."))
        }
    }

    routing {
        get("/") {
            call.respond(HttpStatusCode.OK)
        }
    }
}