package dev.ajkneisl.home.printer.service

/** Authorization for the websocket. */
object Authorization {
    /** Authorize [id] with [token]. */
    fun authorize(id: String, token: String): Boolean {
        val auth = try {
            System.getenv("auth.$id")
        } catch (ex: Exception) {
            return false
        }

        return auth == token
    }
}
