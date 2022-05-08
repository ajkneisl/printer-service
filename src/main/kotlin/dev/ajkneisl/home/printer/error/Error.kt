package dev.ajkneisl.home.printer.error

/** Generic error. */
open class ServerError(override val message: String?) : Throwable()

/** When there's an issue printing something. */
class PrintError : ServerError("There was an issue printing that message!")

/** An error when authorization is missing or failed. */
class AuthorizationError : ServerError("You are not authorized for this!@")
