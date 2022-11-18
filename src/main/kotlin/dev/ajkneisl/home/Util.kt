package dev.ajkneisl.home

import dev.ajkneisl.home.error.AuthorizationError
import dev.ajkneisl.printerlib.Print
import dev.ajkneisl.printerlib.PrintRequest
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import org.json.JSONObject

/** The store of secrets. */
private val secretStore: JSONObject by lazy { JSONObject(System.getenv("SECRETS")) }

/** Get a secret by it's key */
fun getSecret(key: String): String {
    return secretStore.getString(key) ?: ""
}

/** The required token make requests. */
val AUTH_TOKEN by lazy { getSecret("AUTH_TOKEN_DEF") }

/** Authorizes a request. */
fun ApplicationCall.authorize() {
    if (request.authorization() != "Bearer $AUTH_TOKEN") throw AuthorizationError()
}

/** Web client for misc requests. */
val WEB_CLI = HttpClient(CIO)

/** Print request. */
fun Print.print() = PrintHandler.print(PrintRequest(this))
