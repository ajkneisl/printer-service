package dev.ajkneisl.home.printer

import dev.ajkneisl.printerlib.PrintDefaults
import dev.ajkneisl.printerlib.PrintText
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.json.JSONObject

private fun parseAttachmentsSafely(rawData: String): List<JSONObject> {
    val data =
        try {
            JSONObject(rawData)
        } catch (ex: Exception) {
            return listOf()
        }

    val attachments = mutableListOf<JSONObject>()

    data.keys().forEach { attachment ->
        try {
            val attachmentObject = data.getJSONObject(attachment)
            attachments.add(attachmentObject)
        } catch (ex: Exception) {}
    }

    return attachments.toList()
}

fun Application.configureEmailAlerts() {
    routing {
        post("/email") {
            val params = call.receiveParameters()

            PrintHandler.print(
                PrintText(PrintDefaults.TITLE, 0, params["subject"] ?: "No subject."),
                PrintText(PrintDefaults.SUB_TITLE, 0, params["from"] ?: "No sender."),
                PrintText(PrintDefaults.DEFAULT, 2, params["text"] ?: ""),
                PrintText(PrintDefaults.DEFAULT, 0, params["to"] ?: "Unknown receiver."),
                PrintText(
                    PrintDefaults.DEFAULT,
                    0,
                    "Attached:",
                    *parseAttachmentsSafely(params["attachment-info"] ?: "{}")
                        .map { json -> json.getString("filename") }
                        .toTypedArray()
                )
            )
        }
    }
}
