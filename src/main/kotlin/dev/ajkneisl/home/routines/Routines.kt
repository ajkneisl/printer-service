package dev.ajkneisl.home.routines

import dev.ajkneisl.home.WEB_CLI
import dev.ajkneisl.home.print
import dev.ajkneisl.printerlib.Justification
import dev.ajkneisl.printerlib.PrintDefaults
import dev.ajkneisl.printerlib.PrintOptions
import dev.ajkneisl.printerlib.PrintText
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import org.json.JSONObject

@RegisterRoutine
class Weather : Routine("weather", "/weather") {
    override suspend fun invoke(call: ApplicationCall) {
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

        dev.ajkneisl.printerlib.print {
            defaultStyle(
                PrintOptions(
                    underline = false,
                    bold = false,
                    justification = Justification.LEFT,
                    fontSize = 1,
                    font = 0,
                    whiteOnBlack = false
                )
            )

            line("Weather", style = PrintDefaults.TITLE)
            line(date, style = PrintDefaults.SUB_TITLE)

            splitLine {
                segment("Sunrise: ${formatter.format(sunrise)}", "Sunset: ${formatter.format(sunset)}")
                segment("Temp: ${current.getInt("temp")}F", "Feels Like: ${current.getInt("feels_like")}F")
                segment("High: ${daily.getJSONObject("temp").getInt("max")}F", "Low: ${daily.getJSONObject("temp").getInt("min")}F")

                feed = 2
            }

            line {
                style = PrintDefaults.SUB_TITLE
                text += "Weather: ${weather.getJSONObject(0).getString("description")}"
            }
        }.print()

        call.respond(HttpStatusCode.OK)
    }
}
