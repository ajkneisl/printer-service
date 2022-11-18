package dev.ajkneisl.home.routines

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.collections.*
import org.reflections.Reflections
import java.util.logging.Logger

/** Handles routines. */
object RoutineHandler {
    private val LOGGER: Logger = Logger.getLogger("routines")

    /** All routines */
    private val routines = ConcurrentSet<Routine>()

    /** Register all routines that are using [RegisterRoutine]. */
    fun registerRoutines() {
        Reflections("dev.ajkneisl.home.printer")
            .getTypesAnnotatedWith(RegisterRoutine::class.java)
            .map { clazz -> clazz.getConstructor().newInstance() as Routine }
            .forEach { routine -> registerRoutine(routine) }
    }

    /** Register a [routine] */
    fun registerRoutine(routine: Routine) {
        LOGGER.info("Registered routine: ${routine.name} (${routine.route})")

        routines.add(routine)
    }

    /** Register an inline routine. */
    fun registerRoutine(name: String, route: String, invoke: ApplicationCall.() -> Unit) {
        registerRoutine(
            object : Routine(name, route) {
                override suspend fun invoke(call: ApplicationCall) {
                    invoke.invoke(call)
                }
            }
        )
    }

    /** Hook all [routines] into [route]. */
    fun hookRoutines(route: Route) {
        LOGGER.info("Hooking ${routines.size} routines.")

        route.route("/routines") {
            routines.forEach { routine -> get(routine.route) { routine.invoke(call) } }
        }
    }
}
