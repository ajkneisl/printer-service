package dev.ajkneisl.home.printer.todoist.obj

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A Todoist project. */
@Serializable
data class Project(
    val id: Long,
    val name: String,
    @SerialName("comment_count") val commentCount: Int,
    val color: Int,
    val shared: Boolean,
    val order: Int? = -1,
    val favorite: Boolean,
    val url: String
)
