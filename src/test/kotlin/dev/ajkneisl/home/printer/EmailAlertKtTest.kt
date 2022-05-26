package dev.ajkneisl.home.printer

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.server.testing.*
import kotlin.test.Test

class EmailAlertKtTest {
    @Test
    fun testPostPrinterEmail() = testApplication {
        client.post("/printer/email") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("subject", "subject")
                        append("from", "from")
                        append("text", "text")
                        append("to", "to")
                        append("attachment-info", "{}")
                    }
                )
            )
        }.apply {

        }
    }
}
