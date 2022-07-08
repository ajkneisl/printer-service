package dev.ajkneisl.home.printer

import dev.ajkneisl.printerlib.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.regex.Pattern
import kotlin.math.roundToInt
import org.json.JSONObject
import org.jsoup.Jsoup

/**
 * Parses through the attachments in an email with understanding of malformed JSON or missing
 * attachments altogether.
 */
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

/** Handles a purchase alert with HTML body. */
fun handlePurchaseAlert(params: Parameters) {
    val text =
        Jsoup.parse(params["html"] ?: "<p>No body.</p>").getElementsByTag("p").firstOrNull()?.text()

    val separatePrintLines = mutableListOf<Pair<String, String>>()

    if (text != null) {
        val regex = Pattern.compile("(\\d+(?:\\.\\d+)?)")
        val matcher = regex.matcher(text)
        val numbers = mutableListOf<Double>()

        while (matcher.find()) {
            val num = matcher.group(1).toDoubleOrNull()

            if (num != null) numbers.add(num)
        }

        if (numbers.isNotEmpty()) {
            separatePrintLines.add("Card" to numbers.first().roundToInt().toString())
            separatePrintLines.add("Cost" to "$${numbers.last()}")
        }
    }

    PrintHandler.print(
        PrintImage("https://i.ibb.co/zh9J8zr/output-onlinepngtools.png", Justification.CENTER, 2),
        PrintText(
            PrintOptions(
                underline = false,
                bold = false,
                justification = Justification.LEFT,
                fontSize = 2,
                font = 1,
                whiteOnBlack = false
            ),
            1,
            text ?: "No body"
        ),
        SeparatePrintText(PrintDefaults.DEFAULT, 2, *separatePrintLines.toTypedArray())
    )
}

/** Responds to `POST /email` when PostGrid sends a webhook request. */
fun Route.emailRouting() {
    post("/email") {
        val params = call.receiveParameters()

        if (params["to"].equals("purchase_alert@printer.ajkn.us", true)) {
            handlePurchaseAlert(params)
        } else {
            PrintHandler.print(
                PrintText(PrintDefaults.TITLE, 0, params["subject"] ?: "No subject."),
                PrintText(PrintDefaults.SUB_TITLE, 0, params["from"] ?: "No sender."),
                PrintText(PrintDefaults.DEFAULT, 2, "Text Body:", params["text"] ?: ""),
                PrintText(PrintDefaults.DEFAULT, 2, "Html Body:", params["html"] ?: ""),
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

        call.respond(HttpStatusCode.OK)
    }
}
