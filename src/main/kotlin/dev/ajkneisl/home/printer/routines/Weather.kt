package dev.ajkneisl.home.printer.routines

import dev.ajkneisl.home.printer.PrintHandler
import dev.ajkneisl.home.printer.WEB_CLI
import dev.ajkneisl.printerlib.PrintDefaults
import dev.ajkneisl.printerlib.PrintText
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import org.json.JSONObject

/** Prints information about the current weather. */
suspend fun weatherRoutine() {
    val response: String =
        WEB_CLI.get(
                "https://api.openweathermap.org/data/2.5/onecall$LOCATION&units=imperial&appid=${WEATHER_API}"
            )
            .body()

    val json = JSONObject(response)
    val current = json.getJSONObject("current")
    val weather = current.getJSONArray("weather")
    val daily = json.getJSONArray("daily").getJSONObject(0)

    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
    val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(LocalDate.now())

    val sunrise =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(current.getLong("sunrise")),
            TimeZone.getDefault().toZoneId()
        )
    val sunset =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(current.getLong("sunset")),
            TimeZone.getDefault().toZoneId()
        )

    PrintHandler.print(
        PrintText(PrintDefaults.TITLE, 0, "Weather"),
        PrintText(PrintDefaults.SUB_TITLE, 0, date),
        PrintText(
            PrintDefaults.DEFAULT,
            0,
            "Sunrise: ${formatter.format(sunrise)}",
            "Sunset: ${formatter.format(sunset)}",
            "Current Temperature: ${current.getInt("temp")}F",
            "Feels Like: ${current.getInt("feels_like")}F",
            "High: ${daily.getJSONObject("temp").getInt("max")}F",
            "Low: ${daily.getJSONObject("temp").getInt("min")}F",
            "Weather: ${weather.getJSONObject(0).getString("description")}"
        )
    )
}
