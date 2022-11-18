package dev.ajkneisl.home.handle.todoist.obj

import kotlinx.serialization.SerialName

/** a Todoist Task. Received through Todoist API. */
@kotlinx.serialization.Serializable
data class Task(
    val id: Long,
    @SerialName("project_id") val projectId: Long,
    @SerialName("section_id") val sectionId: Long,
    val content: String,
    val description: String,
    val completed: Boolean,
    @SerialName("label_ids") val labelIds: List<Long>,
    @SerialName("parent_id") val parentId: Long? = null,
    val order: Int,
    val priority: Int,
    val due: Due? = null,
    val url: String,
    @SerialName("comment_count") val commentCount: Int,
    val assignee: Int? = null,
    val assigner: Int,
    val creator: Int,
    val created: String? = null
) {
    override fun equals(other: Any?): Boolean {
        return other is Task && other.id == this.id
    }
}