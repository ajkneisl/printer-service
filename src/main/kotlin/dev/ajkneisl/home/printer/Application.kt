package dev.ajkneisl.home.printer

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import dev.ajkneisl.home.printer.error.ServerError
import dev.ajkneisl.home.printer.routines.routineRouting
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val startTime = System.currentTimeMillis()

fun getUptime() = System.currentTimeMillis() - startTime

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress(
    "unused"
) // application.conf references the main function. This annotation prevents the IDE from marking it
// as unused.
fun Application.module() {
    val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger: Logger = loggerContext.getLogger("org.mongodb.driver")
    rootLogger.level = Level.OFF

    install(AutoHeadResponse)
    install(DoubleReceive)
    install(CachingHeaders) {}
    install(DefaultHeaders) { header("X-Server", "ajkn.printer-service") }
    install(ContentNegotiation) { json() }

    install(StatusPages) {
        exception<ServerError>() { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("response" to cause.message))
        }

        exception { call: ApplicationCall, cause: Exception ->
            cause.printStackTrace()

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("response" to "There was an issue on our end.")
            )
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.Authorization)
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        callIdMdc("call-id")
    }

    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { callId: String -> callId.isNotEmpty() }
    }

    routing {
        route("/printer") {
            routineRouting()
            emailRouting()
            todoistRouting()

            get("/") {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "server" to "ajkn.printer-service",
                        "version" to "0.1.4",
                        "uptime" to "${getUptime()}"
                    )
                )
            }
            
            get("/health") {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "server" to "ajkn.printer-service",
                        "version" to "0.1.4",
                        "uptime" to "${getUptime()}"
                    )
                )
            }
        }
    }
}
