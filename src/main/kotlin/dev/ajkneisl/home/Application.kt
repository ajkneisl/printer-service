package dev.ajkneisl.home

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import dev.ajkneisl.home.email.EmailHandler.emailRouting
import dev.ajkneisl.home.error.Empty
import dev.ajkneisl.home.error.InvalidBody
import dev.ajkneisl.home.error.ServerError
import dev.ajkneisl.home.routines.RoutineHandler
import dev.ajkneisl.home.handle.todoist.Todoist.todoistRouting
import dev.ajkneisl.printerlib.PrintLine
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/** The start time of the service. */
private val startTime = System.currentTimeMillis()

/** JSON Instance */
val JSON = Json { ignoreUnknownKeys = true }

/** Calculate how long the service has been running in MS */
fun getUptime() = System.currentTimeMillis() - startTime

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress(
    "unused"
)
fun Application.module() {
    val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger: Logger = loggerContext.getLogger("org.mongodb.driver")
    rootLogger.level = Level.OFF

    RoutineHandler.registerRoutines()

    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, code ->
            call.respond(code, mapOf("response" to "That endpoint could not be found."))
        }

        exception<ServerError>() { call, cause ->
            call.respond(cause.status, mapOf("response" to cause.message))
        }

        exception { call: ApplicationCall, cause: Throwable ->
            cause.printStackTrace()

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("response" to "There was an issue on our end.")
            )
        }
    }

    install(AutoHeadResponse)
    install(DoubleReceive)
    install(DefaultHeaders) { header("X-Server", "ajkn.home") }
    install(ContentNegotiation) { json(JSON) }

    install(io.ktor.server.plugins.cors.routing.CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)

        allowHeader(HttpHeaders.Authorization)
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    routing {
        route("/printer") {
            RoutineHandler.hookRoutines(this)
            emailRouting()
            todoistRouting()

            get { call.respond(HttpStatusCode.OK) }

            put("/print") {
                call.authorize()

                val obj: List<PrintLine> =
                    try {
                        val body = call.receiveText()
                        JSON.decodeFromString(ListSerializer(PrintLine.serializer()), body)
                    } catch (ex: Throwable) {
                        throw InvalidBody()
                    }

                if (obj.isEmpty())
                    throw Empty()

                PrintHandler.print(*obj.toTypedArray())

                call.respond(HttpStatusCode.OK, mapOf("payload" to "The print has been queued."))
            }
        }
    }
}
