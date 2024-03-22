package co.adaptive.euroexplorer.dto

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val question: String,
    val correct_answer: String,
    val level: Int,
)