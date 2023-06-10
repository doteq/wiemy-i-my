package me.doteq.dolinabaryczy.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PoiQuest(
    val id: String,
    val title: String,
    val subtitle: String,
    val displayContent: Boolean,
    val fact: String?,
    val content: String?,
    val questions: List<QuizQuestion>
)
