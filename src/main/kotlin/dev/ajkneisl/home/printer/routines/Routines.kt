package dev.ajkneisl.home.printer.routines

import dev.ajkneisl.home.printer.authorize
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.routineRouting() {
    routing {
        route("/routines") {
            put("/morning") {
                call.authorize()
                goodMorning()
                call.respond(HttpStatusCode.OK)
            }

            put("/weather") {
                call.authorize()
                weatherRoutine()
                call.respond(HttpStatusCode.OK)
            }

            put("/away") {
                call.authorize()
                call.respond(HttpStatusCode.OK)
            }

            put("/home") {
                call.authorize()
                homeRoutine()
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun route(s: String, function: () -> Unit) {

}
