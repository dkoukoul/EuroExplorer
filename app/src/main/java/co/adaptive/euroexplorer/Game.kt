package co.adaptive.euroexplorer

import android.content.Context
import android.util.Log
import co.adaptive.euroexplorer.dto.Question
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import java.io.IOException
import kotlin.random.Random

class Game(private val context: Context) {
    companion object {
        private const val TAG = "Game"
        private var score = 0
        private var currentQuestion = 0
        private var questionsAsked = mutableListOf<Int>()
        private var questions = mutableListOf<Question>()

    }

    // Method to load questions from JSON file
    fun loadQuestions() {
        val jsonString: String
        try {
            jsonString = context.assets.open("questions.json").bufferedReader().use { it.readText() }
            val jsonArray = Json.parseToJsonElement(jsonString).jsonArray
            jsonArray.forEach { jsonElement ->
                val jsonObject = jsonElement as JsonObject
                val question = Json.decodeFromString<Question>(jsonObject.toString())
                questions.add(question)
            }

        } catch (ioException: IOException) {
            Log.e(TAG, "Error loading quesitons", ioException)
        }
    }

    // Method to get a question
    fun getQuestion(): String {
        val possibleQuestions = (questions.indices).toMutableList()
        possibleQuestions.removeAll(questionsAsked)
        if (possibleQuestions.isEmpty()) {
            Log.w(TAG, "No more questions to ask")
        }
        currentQuestion = possibleQuestions[Random.nextInt(possibleQuestions.size)]
        questionsAsked.add(currentQuestion)
        return questions[currentQuestion].question
    }

    // Method to check if the given answer is correct
    fun checkAnswer(answer: String): Boolean {
        val correctAnswer = questions[currentQuestion].correct_answer
        updateScore(answer == correctAnswer)
        return answer == correctAnswer
    }

    // Method to update the score, 3 points for correct answer, 1 point for incorrect answer
    fun updateScore(correct: Boolean = false) {
        if (correct) {
            score += 3
        } else {
            score++
        }
    }

    fun getScore(): Int {
        return score
    }
}