package me.doteq.dolinabaryczy.data.models

import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestion(
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>,
)