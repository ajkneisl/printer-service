package dev.ajkneisl.home.handle.todoist.obj

import kotlinx.serialization.SerialName

/** A due date for a Todoist [Task]. */
@kotlinx.serialization.Serializable
data class Due(
    val string: String,
    val date: String,
    val recurring: Boolean,
    @SerialName("datetime") val dateTime: String? = null,
    val timezone: String? = null,
    val lang: String? = null
)