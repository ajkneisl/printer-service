package dev.ajkneisl.home.printer.routines

import dev.ajkneisl.home.printer.WEB_CLI
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

suspend fun weatherRoutine() {
    val response: String =
        WEB_CLI
            .get(
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

//    PrintHandler.print(
//        "Weather",
//        date,
//        qrCode = null,
//        "Sunrise: ${formatter.format(sunrise)}" feed 0,
//        "Sunset: ${formatter.format(sunset)}" feed 1,
//        "Current Temperature: ${current.getInt("temp")}F" feed 0,
//        "Feels Like: ${current.getInt("feels_like")}F" feed 0,
//        "High: ${daily.getJSONObject("temp").getInt("max")}F" feed 0,
//        "Low: ${daily.getJSONObject("temp").getInt("min")}F" feed 1,
//        "Weather: ${weather.getJSONObject(0).getString("description")}" feed 0
//    )
}