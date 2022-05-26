package dev.ajkneisl.home.printer

import dev.ajkneisl.printerlib.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.litote.kmongo.json

class ApplicationKtTest {

    /** Test GET /printer */
    @Test
    fun testRoot() = testApplication {
        client.get("/printer").apply { assertEquals(HttpStatusCode.OK, status) }
    }

    /** Test PUT /printer/print */
    @Test
    fun testCustomPrint() = testApplication {
        val client = createClient {
            defaultRequest {
                header("Authorization", "Bearer ${System.getenv("TEST_TOKEN")}")
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        serializersModule = SerializersModule {
                            contextual(
                                List::class as KClass<List<PrintLine>>,
                                ListSerializer(PrintLine.serializer())
                            )
                        }
                    }
                )
            }
        }

        // test with nothing. should 500
        client.put("/printer/print").apply {
            assertEquals(HttpStatusCode.UnsupportedMediaType, status)
        }

        // test with body, but empty. should 400
        client
            .put("/printer/print") {
                val bodyData: List<PrintLine> = listOf()

                setBody(bodyData)
                contentType(ContentType.Application.Json)
            }
            .apply { assertEquals(HttpStatusCode.ExpectationFailed, status) }

        // test with body
        client
            .put("/printer/print") {
                val bodyData: List<PrintLine> =
                    listOf(
                        PrintQrCode("Test", 1, Justification.CENTER, 0),
                        PrintText(PrintDefaults.DEFAULT, 0, "Test")
                    )

                setBody(bodyData)
                contentType(ContentType.Application.Json)
            }
            .apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(mapOf("payload" to "The print has been queued."), body())
            }
    }

    /** Test GET /printer/health */
    @Test
    fun testGetPrinterHealth() = testApplication {
        client.get("/printer/health").apply { assertEquals(HttpStatusCode.OK, status) }
    }
}
