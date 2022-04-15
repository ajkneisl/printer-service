package dev.ajkneisl.printer.obj

data class Print(
    val id: String,
    val createdAt: Long,
    val title: String,
    val subtitle: String,
    val lines: List<PrintLine>,
    val qrCode: String?
)