package dev.ajkneisl.home.printer

import dev.ajkneisl.home.printer.error.AuthorizationError
import dev.ajkneisl.printerlib.*
import dev.ajkneisl.home.printer.getSecret
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
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.json.JSONObject

/** a Todoist Task. Received through Todoist API. */
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

/** A due date for a Todoist [Task]. */
@kotlinx.serialization.Serializable
data class Due(
    val string: String,
    val date: String,
    val recurring: Boolean,
    @SerialName("datetime") val dateTime: String? = null,
    val timezone: String? = null,
    val lang: String? = null
)

/** Automatically authorized for Todoist. */
private val TODOIST_WEB_CLI =
    HttpClient(CIO) {
        defaultRequest { header("Authorization", "Bearer ${getSecret("API_KEY_TODOIST")}") }

        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

/** Configure Todoist routing. */
fun Route.todoistRouting() {
    route("/todoist") {
        post {
            val headers = call.request.headers

            if (!headers.contains("X-Todoist-Hmac-SHA256")) throw AuthorizationError()

            val body = JSONObject(call.receiveText())
            val eventData = body.getJSONObject("event_data")

            PrintHandler.print(
                PrintText(PrintDefaults.TITLE, 0, "Todoist"),
                PrintText(PrintDefaults.DEFAULT, 0, body.getString("event_name"))
            )

            call.respond(mapOf("response" to "OK"))
        }

        route("/printout") {
            put {
                call.authorize()
                printout()
                call.respond(HttpStatusCode.OK)
            }

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
        }
    }
}

/** Get all tasks from Todoist API. */
suspend fun getTasks(): List<Task> {
    return TODOIST_WEB_CLI.get("https://api.todoist.com/rest/v1/tasks").body()
}

/** Use [getTasks] to find tasks that are due today. */
suspend fun getDueToday(): List<Task> {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)

    return getTasks()
        .filter { task -> !task.completed && task.due != null }
        .filter { task ->
            val split = task.due?.date?.split("-")!!

            split[1].toIntOrNull() == month + 1 && split[2].toIntOrNull() == today
        }
}

/** Find tasks that aren't completed and have a due date. */
private suspend fun getUncompletedTasks(): List<Task> {
    return getTasks().filter { task -> !task.completed && task.due != null }
}

/** Find overdue tasks from [tasks]. */
fun getOverdue(tasks: List<Task>): List<Task> {
    return tasks.filter(::isOverdue)
}

/** Check if [task] is overdue. */
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
        tasks
            .toMutableList()
            .apply { removeAll(overdue) }
            .sortedByDescending { task -> task.priority }

    val lines = mutableListOf<PrintLine>()
    val title =
        PrintOptions(
            false,
            bold = false,
            justification = Justification.CENTER,
            fontSize = 2,
            font = 0,
            whiteOnBlack = false
        )

    lines.add(PrintText(title, 1, "OVERDUE (${overdue.size})"))
    overdue.forEachIndexed { index, task ->
        val size = if (index == overdue.size - 1) 1 else 0

        lines.add(PrintText(PrintDefaults.DEFAULT, size, "${task.due?.date}: ${task.content}"))
    }

    lines.add(PrintText(title, 1, "DUE (${due.size})"))
    due.forEachIndexed { index, task ->
        val size = if (index == due.size - 1) 1 else 0

        lines.add(PrintText(PrintDefaults.DEFAULT, size, "${task.due?.date}: ${task.content}"))
    }

    PrintHandler.print(
        PrintImage("https://logodix.com/logo/1851750.png", Justification.CENTER, 1),
        *lines.toTypedArray(),
        PrintQrCode("https://todoist.com/app/today", 7, Justification.CENTER, 1)
    )
}

/** Individually printout all tasks with a due date. */
suspend fun printoutAll() {
    val tasks =
        getTasks()
            .filter { task -> !task.completed && task.due != null } // must have a due date
            .map { task -> formatTask(task) }

    PrintHandler.batchPrint(tasks)
}

/** Print out [task]. */
private fun formatTask(task: Task): Print {
    val lines = mutableListOf<PrintLine>()

    if (task.description.isNotBlank())
        lines.add(PrintText(PrintDefaults.DEFAULT, 2, task.description))

    if (isOverdue(task))
        lines.add(
            PrintText(
                PrintOptions(
                    underline = false,
                    bold = true,
                    justification = Justification.CENTER,
                    fontSize = 2,
                    font = 0,
                    whiteOnBlack = false
                ),
                0,
                "Overdue"
            )
        )

    lines.add(
        PrintText(
            PrintDefaults.DEFAULT,
            1,
            "Due: ${task.due?.date ?: "no due date"}",
            "Priority: ${task.priority}",
            "Project: ${task.projectId}",
            "Created At: ${task.created}",
            "Due at: ${task.due?.date ?: "No due date."}"
        )
    )

    return Print(
        UUID.randomUUID().toString(),
        System.currentTimeMillis(),
        listOf(
            PrintImage("https://logodix.com/logo/1851750.png", Justification.CENTER, 1),
            PrintText(PrintDefaults.SUB_TITLE, 0, task.content),
            *lines.toTypedArray(),
            PrintQrCode(task.url, 7, Justification.CENTER, 1)
        )
    )
}

/** Individually printout all tasks. */
suspend fun overduePrintout() {
    val tasks = getOverdue(getUncompletedTasks()).map { task -> formatTask(task) }

    PrintHandler.batchPrint(tasks)
}
