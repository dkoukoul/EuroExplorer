package co.adaptive.euroexplorer

import android.content.Context
import android.location.Address
import android.util.Log
import co.adaptive.euroexplorer.dto.Country
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
        private var questionsCapitalsAsked = mutableListOf<Int>()
        private var questions = mutableListOf<Question>()
        private var countries = mutableListOf<Country>()
    }

    var gameType: GameTypes = GameTypes.CAPITAL

    init {
        loadQuestions()
        loadCountries()
        init()
    }

    fun init() {
        val sharedPreferencesHelper = SharedPreferencesHelper(context)
        gameType = sharedPreferencesHelper.getGameType()
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
            Log.e(TAG, "Error loading questions", ioException)
        }
    }

    private fun loadCountries() {
        val jsonString: String
        try {
            jsonString = context.assets.open("countries.json").bufferedReader().use { it.readText() }
            val jsonArray = Json.parseToJsonElement(jsonString).jsonArray
            jsonArray.forEach { jsonElement ->
                val jsonObject = jsonElement as JsonObject
                val country = Json.decodeFromString<Country>(jsonObject.toString())
                countries.add(country)
            }
        } catch (ioException: IOException) {
            Log.e(TAG, "Error loading countries", ioException)
        }
    }

    // Method to get a question
    fun getQuestion(): String {
        return if (gameType == GameTypes.CAPITAL) {
            getQuestionCapital()
        } else if (gameType == GameTypes.COUNTRY) {
            getCountryQuestion()
        } else {
            Log.e(TAG, "Invalid game type")
            ""
        }
    }

    private fun getCountryQuestion(): String {
        val possibleQuestions = (questions.indices).toMutableList()
        possibleQuestions.removeAll(questionsAsked)
        if (possibleQuestions.isEmpty()) {
            Log.w(TAG, "No more questions to ask")
        }
        currentQuestion = possibleQuestions[Random.nextInt(possibleQuestions.size)]
        questionsAsked.add(currentQuestion)
        return questions[currentQuestion].question
    }

    private fun getQuestionCapital(): String {
        var question = context.getString(R.string.question_capital)
        val possibleQuestions = (countries.indices).toMutableList()
        possibleQuestions.removeAll(questionsCapitalsAsked)
        if (possibleQuestions.isEmpty()) {
            Log.w(TAG, "No more questions to ask")
        }
        currentQuestion = possibleQuestions[Random.nextInt(possibleQuestions.size)]
        question += " " + countries[currentQuestion].article
        return question
    }

    private fun getCurrentCountryName(): String {
        return countries[currentQuestion].name
    }

    fun getCurrentCountry(): Country? {
        return countries[currentQuestion]
    }

    // Method to check if the given answer is correct
    fun checkAnswer(answer: String): Pair<Boolean, String> {
        return checkAnswerCountry(answer)
    }

    fun checkAnswers(answers: List<Address>): Pair<Boolean, String> {
        val correctAnswer = countries[currentQuestion].capital
        if (gameType == GameTypes.CAPITAL) {
            for (address in answers) {
                if ((!address.adminArea.isNullOrEmpty() && checkAnswerCapital(address.adminArea)) ||
                    (!address.subAdminArea.isNullOrEmpty() && checkAnswerCapital(address.subAdminArea)) ||
                    (!address.locality.isNullOrEmpty() && checkAnswerCapital(address.locality)) ||
                    (!address.featureName.isNullOrEmpty() && checkAnswerCapital(address.featureName)) ||
                    (!address.subLocality.isNullOrEmpty() && checkAnswerCapital(address.subLocality))){

                    setScore(true)
                    return Pair(true, correctAnswer)
                }
            }
            // No correct answer found
            setScore(false)
            return Pair(false,correctAnswer)
        } else {
            Log.e(TAG, "Invalid game type")
            return Pair(false,correctAnswer)
        }
    }
    private fun checkAnswerCapital(answer: String): Boolean {
        val correctAnswer = countries[currentQuestion].capital
        if (answer.contains(correctAnswer)) {
            return true
        }

        return false
    }

    private fun checkAnswerCountry(answer: String): Pair<Boolean, String> {
        val correctAnswer = questions[currentQuestion].correct_answer
        setScore(answer == correctAnswer)
        return Pair(answer == correctAnswer, correctAnswer)
    }

    // Method to update the score, 3 points for correct answer, 1 point for incorrect answer
    private fun setScore(correct: Boolean = false) {
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