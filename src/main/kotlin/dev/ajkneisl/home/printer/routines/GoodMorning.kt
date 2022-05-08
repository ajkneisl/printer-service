package dev.ajkneisl.home.printer.routines

import dev.ajkneisl.home.printer.PrintHandler
import dev.ajkneisl.home.printer.WEB_CLI
import dev.ajkneisl.home.printer.getDueToday
import dev.ajkneisl.printerlib.Justification
import dev.ajkneisl.printerlib.PrintDefaults
import dev.ajkneisl.printerlib.PrintOptions
import dev.ajkneisl.printerlib.PrintText
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt
import org.json.JSONObject

/** Location for the OpenWeatherMap */
val LOCATION: String by lazy { System.getenv("LOCATION") }

/** The API for the OpenWeatherMap */
val WEATHER_API: String by lazy { System.getenv("WEATHER_API") }

/** Gives weather information and tasks due today. */
suspend fun goodMorning() {
    val response: String =
        WEB_CLI.get(
                "https://api.openweathermap.org/data/2.5/onecall$LOCATION&units=imperial&appid=${WEATHER_API}"
            )
            .body()

    val json = JSONObject(response)
    val current = json.getJSONObject("current")
    val weather = current.getJSONArray("weather")

    val weatherStatus =
        if (!weather.isEmpty)
            "${weather.getJSONObject(0).getString("description").capitalize()}, ${current.getDouble("temp").roundToInt()}F"
        else "No weather info."

    val dueToday = getDueToday().sortedByDescending { task -> task.priority }

    val dueTodayMessage =
        if (dueToday.isEmpty()) {
            "No tasks due."
        } else {
            "${getDueToday().size} task(s) due today."
        }

    val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(LocalDate.now())

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
        PrintText(PrintDefaults.TITLE, 0, "Good Morning!"),
        PrintText(PrintDefaults.SUB_TITLE, 0, date),
        PrintText(subHeader, 0, "Weather"),
        PrintText(PrintDefaults.DEFAULT, 1, weatherStatus),
        PrintText(subHeader, 0, dueTodayMessage),
        PrintText(PrintDefaults.DEFAULT, 1, *dueToday.map { task -> task.content }.toTypedArray()),
        PrintText(PrintDefaults.SUB_TITLE, 0, "Have a good day.")
    )
}
