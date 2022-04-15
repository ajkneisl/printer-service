package dev.ajkneisl.home.printer.routines

import dev.ajkneisl.home.printer.*
import dev.ajkneisl.home.printer.PrintHandler.feed
import dev.ajkneisl.printer.obj.PrintLine
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

suspend fun homeRoutine() {
    val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(LocalDate.now())

    val dueToday = getDueToday()
    val overdue = getOverdue(getTasks()).filterNot(dueToday::contains)
    val totalDue = dueToday.size + overdue.size
    val dueTodayMessage = if (totalDue == 0)
        "You have nothing due today!"
    else
        "You have $totalDue task(s) due today."

    PrintHandler.print(
        "Welcome Home",
        date,
        qrCode = null,
        PrintLine(dueTodayMessage, 0, title = true),
        *(overdue).map { task -> "OVERDUE: ${task.content}" feed 0 }.toTypedArray(),
        *(dueToday).map { task -> task.content feed 0 }.toTypedArray(),
        "" feed 1,
        PrintLine("Have a good rest of your day.", 0, bold = true)
    )
    weatherRoutine()
}