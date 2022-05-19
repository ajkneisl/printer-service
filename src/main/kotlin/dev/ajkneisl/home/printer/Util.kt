package dev.ajkneisl.home.printer

import dev.ajkneisl.home.printer.error.AuthorizationError
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import org.json.JSONObject;

private val secretStore: JSONObject by lazy {
    JSONObject(System.getenv("SECRETS"))
}

/** Get a secret by it's key */
fun getSecret(key: String): String {
    return secretStore.getString(key) ?: ""
}

/** The required token make requests. */
private val AUTH_TOKEN by lazy { getSecret("AUTH_TOKEN_DEF") }

/** Authorizes a request. */
fun ApplicationCall.authorize() {
    if (request.authorization() != "Bearer $AUTH_TOKEN") throw AuthorizationError()
}

/** Web client for misc requests. */
val WEB_CLI = HttpClient(CIO)
