package dev.ajkneisl.home.printer

import dev.ajkneisl.home.printer.plugins.configureHTTP
import dev.ajkneisl.home.printer.plugins.configureMonitoring
import dev.ajkneisl.home.printer.plugins.configureRouting
import dev.ajkneisl.home.printer.plugins.configureSerialization
import dev.ajkneisl.home.printer.routines.routineRouting
import dev.ajkneisl.home.printer.service.registerWebsocket
import io.ktor.server.application.*

private val startTime = System.currentTimeMillis()

fun getUptime() = System.currentTimeMillis() - startTime

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureRouting()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureTodoist()
    routineRouting()
    configureEmailAlerts()
    registerWebsocket()
}
