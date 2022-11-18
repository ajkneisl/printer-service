package dev.ajkneisl.home.email

import dev.ajkneisl.home.print
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

object EmailHandler {
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
            Jsoup.parse(params["html"] ?: "<p>No body.</p>")
                .getElementsByTag("p")
                .firstOrNull()
                ?.text()

        print {
                image("https://i.ibb.co/zh9J8zr/output-onlinepngtools.png", feed = 2)
                line {
                    style =
                        PrintOptions(
                            underline = false,
                            bold = false,
                            justification = Justification.LEFT,
                            fontSize = 2,
                            font = 1,
                            whiteOnBlack = false
                        )

                    text(text ?: "No body.")
                }

                splitLine {
                    if (text != null) {
                        val regex = Pattern.compile("(\\d+(?:\\.\\d+)?)")
                        val matcher = regex.matcher(text)
                        val numbers = mutableListOf<Double>()

                        while (matcher.find()) {
                            val num = matcher.group(1).toDoubleOrNull()

                            if (num != null) numbers.add(num)
                        }

                        if (numbers.isNotEmpty()) {
                            segment("Card", numbers.first().roundToInt().toString())
                            segment("Cost", "$${numbers.last()}")
                        }
                    }
                }
            }
            .print()
    }

    private val handlers = mutableListOf<AddressHandler>()
    private val defaultHandler =
        AddressHandler("*") { params ->
            print {
                    line(params["subject"] ?: "No subject.", style = PrintDefaults.TITLE)
                    line(params["from"] ?: "No  sender", style = PrintDefaults.SUB_TITLE)
                    line(params["text"] ?: "", feed = 2)
                    line(params["to"] ?: "Unknown receiver.")

                    line {
                        text("Attached:")

                        parseAttachmentsSafely(params["attachment-info"] ?: "{}").forEach { json ->
                            text(json.getString("filename"))
                        }
                    }
                }
                .print()
        }

    /** Register [handler]. */
    fun registerHandler(handler: AddressHandler) {
        handlers.add(handler)
    }

    init {
        registerHandler(AddressHandler("purchase_alert@printer.ajkn.us", this::handlePurchaseAlert))
    }

    /** Responds to `POST /email` when PostGrid sends a webhook request. */
    fun Route.emailRouting() {
        post("/email") {
            val params = call.receiveParameters()
            val to = params["to"] ?: "*"
            var handler = handlers.find { handler -> handler.address.equals(to, true) }

            if (handler == null)
                handler = handlers.find { han -> han.address == "*" } ?: defaultHandler

            handler.handle.invoke(params)

            if (params["to"].equals("purchase_alert@printer.ajkn.us", true)) {
                handlePurchaseAlert(params)
            } else {
                print {
                        line(params["subject"] ?: "No subject.", style = PrintDefaults.TITLE)
                        line(params["from"] ?: "No  sender", style = PrintDefaults.SUB_TITLE)
                        line(params["text"] ?: "No body.", feed = 2)
                        line("Sent to: ${params["to"]}")

                        val attachments = parseAttachmentsSafely(params["attachment-info"] ?: "{}")

                        if (attachments.isNotEmpty()) {
                            line {
                                text("Attachments:")

                                attachments.forEach { attach -> text(attach.getString("filename")) }
                            }
                        }
                    }
                    .print()
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}
