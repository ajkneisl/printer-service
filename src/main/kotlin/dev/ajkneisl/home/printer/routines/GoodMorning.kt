package dev.ajkneisl.home.printer.routines

import dev.ajkneisl.home.printer.WEB_CLI
import dev.ajkneisl.home.printer.getDueToday
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

val LOCATION: String by lazy {
    System.getenv("LOCATION")
}

val WEATHER_API: String by lazy {
    System.getenv("WEATHER_API")
}

suspend fun goodMorning() {
    val response: String =
        WEB_CLI.get(
            "https://api.openweathermap.org/data/2.5/onecall$LOCATION&units=imperial&appid=${WEATHER_API}"
        ).body()

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

//    PrintHandler.print(
//        "Good Morning",
//        date,
//        qrCode = null,
//        PrintLine("Weather", 0, title = true),
//        weatherStatus feed 1,
//        PrintLine(dueTodayMessage, 0, title = true),
//        *dueToday.map { task -> task.content feed 0 }.toTypedArray(),
//        "" feed 1,
//        PrintLine("Have a good day.", 0, bold = true)
//    )
}