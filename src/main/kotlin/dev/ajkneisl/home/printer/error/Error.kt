package dev.ajkneisl.home.printer.error

import io.ktor.http.*

/** Generic error. */
open class ServerError(
    override val message: String?,
    val status: HttpStatusCode = HttpStatusCode.BadRequest
) : Throwable()

/** When there's an issue printing something. */
class PrintError :
    ServerError(
        "There was an issue printing that message!",
        status = HttpStatusCode.InternalServerError
    )

/** An error when authorization is missing or failed. */
class AuthorizationError :
    ServerError("You are not authorized for this!", status = HttpStatusCode.Forbidden)

/** An error when content is not included in the request. */
class Empty :
    ServerError(
        "You must include content in your request!",
        status = HttpStatusCode.ExpectationFailed
    )

/** When an invalid body type is presented, a body is missing, or malformed JSON is presented. */
class InvalidBody : ServerError("Invalid body!", status = HttpStatusCode.UnsupportedMediaType)
