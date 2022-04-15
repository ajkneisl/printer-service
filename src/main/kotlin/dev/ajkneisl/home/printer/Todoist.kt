package dev.ajkneisl.home.printer

import dev.ajkneisl.home.printer.PrintHandler.feed
import dev.ajkneisl.home.printer.error.AuthorizationError
import dev.ajkneisl.printer.obj.PrintLine
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.util.*

@kotlinx.serialization.Serializable
data class Task(
    val id: Long,
    @SerialName("project_id") val projectId: Long,
    @SerialName("section_id") val sectionId: Long,
    val content: String,
    val description: String,
    val completed: Boolean,
    @SerialName("label_ids") val labelIds: List<Long>,
    @SerialName("parent_id") val parentId: Long? = null,
    val order: Int,
    val priority: Int,
    val due: Due? = null,
    val url: String,
    @SerialName("comment_count") val commentCount: Int,
    val assignee: Int? = null,
    val assigner: Int,
    val creator: Int,
    val created: String? = null
) {
    override fun equals(other: Any?): Boolean {
        return other is Task && other.id == this.id
    }
}

/**
 * A due date for a Todoist [Task].
 */
@kotlinx.serialization.Serializable
data class Due(
    val string: String,
    val date: String,
    val recurring: Boolean,
    @SerialName("datetime") val dateTime: String? = null,
    val timezone: String? = null,
    val lang: String? = null
)

private val TODOIST_WEB_CLI = HttpClient(CIO) {
    defaultRequest {
        header("Authorization", "Bearer ${System.getenv("TODOIST")}")
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureTodoist() {
    routing {
        route("/todoist") {
            post {
                val headers = call.request.headers

                if (!headers.contains("X-Todoist-Hmac-SHA256"))
                    throw AuthorizationError()

                val body = JSONObject(call.receiveText())
                val eventData = body.getJSONObject("event_data")

                PrintHandler.print("Todoist", body.getString("event_name"), "")

                call.respond(mapOf("response" to "OK"))
            }

            route("/printout") {
                put("/all") {
                    call.authorize()
                    printoutAll()
                    call.respond(HttpStatusCode.OK)
                }

                put("/overdue") {
                    call.authorize()
                    overduePrintout()
                    call.respond(HttpStatusCode.OK)
                }

                put {
                    call.authorize()
                    printout()
                    call.respondText(":)")
                }
            }
        }
    }
}

suspend fun getTasks(): List<Task> {
    return TODOIST_WEB_CLI.get("https://api.todoist.com/rest/v1/tasks").body()
}

suspend fun getDueToday(): List<Task> {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)

    return getTasks().filter { task -> !task.completed && task.due != null }.filter { task ->
        val split = task.due?.date?.split("-")!!

        split[1].toIntOrNull() == month + 1 && split[2].toIntOrNull() == today
    }
}

private suspend fun getUncompletedTasks(): List<Task> {
    return getTasks().filter { task -> !task.completed && task.due != null }
}

fun getOverdue(tasks: List<Task>): List<Task> {
    return tasks.filter(::isOverdue)
}

private fun isOverdue(task: Task): Boolean {
    val cal = Calendar.getInstance()
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = cal.get(Calendar.MONTH) + 1

    val dueDate = task.due?.date

    return if (dueDate != null) {
        val split = dueDate.split("-")

        val dueMonth = split[1].toInt()
        val dueDay = split[2].toInt()

        (month > dueMonth) || (month == dueMonth && day >= dueDay)
    } else false
}

/** Overdue and Not due yet printout. */
suspend fun printout() {
    val tasks = getUncompletedTasks()

    val overdue = getOverdue(tasks).sortedByDescending { task -> task.priority }
    val due =
        tasks.toMutableList().apply { removeAll(overdue) }.sortedByDescending { task ->
            task.priority
        }

    val lines = mutableListOf<PrintLine>()

    lines.add("OVERDUE (${overdue.size})" feed 0)
    overdue.forEachIndexed { index, task ->
        val size = if (index == overdue.size - 1) 1 else 0

        lines.add("${task.due?.date}: ${task.content}" feed size)
    }

    lines.add("DUE (${due.size})" feed 0)
    due.forEach { task -> lines.add("${task.due?.date}: ${task.content}" feed 0) }

    PrintHandler.print(
        "Todoist",
        "Task Printout",
        "https://todoist.com/app/today",
        *lines.toTypedArray()
    )
}

suspend fun printoutAll() {
    getTasks()
        .filter { task -> !task.completed && task.due != null } // must have a due date
        .forEach(::printTask)
}

private fun printTask(task: Task) {
    val lines = mutableListOf<PrintLine>()

    if (task.description.isNotBlank()) lines.add(PrintLine(task.description, 2))

    lines.add("Due: ${task.due?.date ?: "no due date"}" feed 0)
    lines.add("Priority: ${task.priority}" feed 0)
    lines.add("Project: ${task.projectId}" feed 0)
    lines.add("Created At: ${task.created}" feed 0)
    lines.add("Due at: ${task.due?.date ?: "No due date."}" feed 0)

    if (isOverdue(task))
        lines.add(PrintLine("Overdue", 0, bold = true))

    PrintHandler.print("Task", task.content, task.url, *lines.toTypedArray())
}

suspend fun overduePrintout() {
    getOverdue(getUncompletedTasks()).forEach(::printTask)
}