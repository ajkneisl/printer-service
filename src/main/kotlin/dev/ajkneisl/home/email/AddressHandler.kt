package dev.ajkneisl.home.email

import io.ktor.http.*

data class AddressHandler(val address: String, val handle: (Parameters) -> Unit)