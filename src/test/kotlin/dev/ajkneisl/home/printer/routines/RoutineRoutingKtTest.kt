package dev.ajkneisl.home.printer.routines

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutineRoutingKtTest {
    @Test
    fun testPutPrinterRoutinesMorning() = testApplication {
        client
            .put("/printer/routines/morning") {
                header("Authorization", "Bearer ${System.getenv("TEST_TOKEN")}")
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun testPutPrinterRoutinesWeather() = testApplication {
        client
            .put("/printer/routines/weather") {
                header("Authorization", "Bearer ${System.getenv("TEST_TOKEN")}")
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun testPutPrinterRoutinesAway() = testApplication {
        client
            .put("/printer/routines/away") {
                header("Authorization", "Bearer ${System.getenv("TEST_TOKEN")}")
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun testPutPrinterRoutinesHome() = testApplication {
        client
            .put("/printer/routines/home") {
                header("Authorization", "Bearer ${System.getenv("TEST_TOKEN")}")
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }
    }
}
