package dev.ajkneisl.home.handle.todoist

import dev.ajkneisl.printerlib.*
import org.json.JSONObject

object Webhook {
    /** Handles reminder:fired */
    suspend fun JSONObject.handleReminderFired(): List<PrintLine> {
        val task = Todoist.getTask(getLong("item_id"))

        return listOf(
            PrintText(PrintDefaults.TITLE, 0, "Reminder"),
            PrintText(PrintDefaults.SUB_TITLE, 1, "Todoist"),
            SeparatePrintText(
                PrintDefaults.DEFAULT,
                0,
                "Task" to task.content,
                "Description" to task.description,
                "Date" to getJSONObject("date").getString("date")
            )
        )
    }

    /** Handles item:added */
    suspend fun JSONObject.handleItemAdded(): List<PrintLine> = itemHandler("New Entry")

    /** Handles item:updated */
    suspend fun JSONObject.handleItemModified(): List<PrintLine> = itemHandler("Entry Modified")

    /** Handles item:completed */
    suspend fun JSONObject.handleItemComplete(): List<PrintLine> = itemHandler("Complete!")

    /** Handles any item request. */
    private suspend fun JSONObject.itemHandler(type: String): List<PrintLine> {
        var dueStr = "No due date"
        if (has("due") && get("due") is JSONObject) {
            dueStr = getJSONObject("due").getString("date")
        }

        return listOf(
            PrintText(PrintDefaults.TITLE, 0, "Todoist"),
            PrintText(PrintDefaults.SUB_TITLE, 1, type),
            SeparatePrintText(
                PrintDefaults.DEFAULT,
                0,
                "Name" to getString("content"),
                "Description" to getString("description"),
                "Priority" to getInt("priority").toString(),
                "Project" to Todoist.getProject(getLong("project_id")).name,
                "Due" to dueStr
            )
        )
    }
}
