package co.adaptive.euroexplorer.dto

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val question: String,
    val options: List<String>,
    val correct_answer: String
)

/*  {
    "question1": "Ποια είναι η πρωτεύουσα ",
    "question2": "Ποια είναι η σημαία ",
    "question3": "Ποιας χώρας είναι αυτή η σημαία;",
    "question4": "Ποια χώρα δεν είναι στην Ευρωπαϊκή Ένωση;"
  },*/