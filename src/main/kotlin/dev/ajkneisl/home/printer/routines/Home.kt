package dev.ajkneisl.home.printer.routines

import dev.ajkneisl.home.printer.*
import dev.ajkneisl.printerlib.Justification
import dev.ajkneisl.printerlib.PrintDefaults
import dev.ajkneisl.printerlib.PrintOptions
import dev.ajkneisl.printerlib.PrintText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Gives information about current due tasks and weather report */
suspend fun homeRoutine() {
    val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(LocalDate.now())

    val dueToday = getDueToday()
    val overdue = getOverdue(getTasks()).filterNot(dueToday::contains)
    val totalDue = dueToday.size + overdue.size
    val dueTodayMessage =
        if (totalDue == 0) "You have nothing due today!"
        else "You have $totalDue task(s) due today."

    val subHeader =
        PrintOptions(
            false,
            bold = false,
            justification = Justification.CENTER,
            fontSize = 2,
            font = 0,
            whiteOnBlack = false
        )

    PrintHandler.print(
        PrintText(PrintDefaults.TITLE, 0, "Welcome Home"),
        PrintText(PrintDefaults.SUB_TITLE, 0, date),
        PrintText(subHeader, 0, dueTodayMessage),
        PrintText(PrintDefaults.DEFAULT, 1, *dueToday.map { task -> task.content }.toTypedArray()),
        PrintText(PrintDefaults.SUB_TITLE, 0, "Have a good rest of your day day.")
    )

    weatherRoutine()
}
