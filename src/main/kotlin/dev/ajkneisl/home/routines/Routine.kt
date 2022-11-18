package dev.ajkneisl.home.routines

import io.ktor.server.application.*

/** A routine. */
abstract class Routine(val name: String, val route: String) {
    abstract suspend fun invoke(call: ApplicationCall)
}
