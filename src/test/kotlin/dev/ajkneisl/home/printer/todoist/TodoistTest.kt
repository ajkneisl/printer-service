package dev.ajkneisl.home.printer.todoist

import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TodoistTest {
    @Test
    fun testPostPrinterTodoist() = testApplication {
//        client TODO: update testing
//            .post("/printer/todoist?auth=${System.getenv("TEST_TOKEN")}") {
//                setBody()
//                contentType(ContentType.Application.Json)
//            }
//            .apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun testPutPrinterTodoistPrintout() = testApplication {
        client
            .put("/printer/todoist/printout") {
                header("Authorization", "Bearer ${System.getenv("TEST_TOKEN")}")
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun testPutPrinterTodoistPrintoutAll() = testApplication {
        client
            .put("/printer/todoist/printout/all") {
                header("Authorization", "Bearer ${System.getenv("TEST_TOKEN")}")
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun testPutPrinterTodoistPrintoutOverdue() = testApplication {
        client
            .put("/printer/todoist/printout/overdue") {
                header("Authorization", "Bearer ${System.getenv("TEST_TOKEN")}")
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }
    }
}
