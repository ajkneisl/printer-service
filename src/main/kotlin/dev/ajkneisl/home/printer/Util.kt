package dev.ajkneisl.home.printer

import dev.ajkneisl.home.printer.error.AuthorizationError
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import io.ktor.server.request.*

private val AUTH_TOKEN by lazy {
    System.getenv("AUTH_TOKEN")
}

fun ApplicationCall.authorize() {
    if (request.authorization() != "Bearer $AUTH_TOKEN")
        throw AuthorizationError()
}

val WEB_CLI = HttpClient(CIO)