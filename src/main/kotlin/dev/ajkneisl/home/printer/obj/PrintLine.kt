package dev.ajkneisl.printer.obj

data class PrintLine(
    val content: String,
    val feed: Int,
    val bold: Boolean = false,
    val title: Boolean = false,
    val underlined: Boolean = false
)