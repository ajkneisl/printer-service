package dev.ajkneisl.home.printer.error

open class ServerError(override val message: String?) : Throwable()

class PrintError : ServerError("There was an issue printing that message!")

class AuthorizationError : ServerError("You are not authorized for this!@")
