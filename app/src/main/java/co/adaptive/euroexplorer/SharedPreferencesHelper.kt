package co.adaptive.euroexplorer

import android.content.Context
import android.content.SharedPreferences
import co.adaptive.euroexplorer.dto.GameLevel
import co.adaptive.euroexplorer.dto.LevelStatus

class SharedPreferencesHelper(context: Context) {
    private val NUMBER_OF_LEVELS = 8
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("co.adaptive.euroexplorer", Context.MODE_PRIVATE)
    private val GAME_LEVEL_NAMES = arrayOf("Level 1", "Level 2", "Level 3", "Level 4", "Level 5", "Level 6", "Level 7", "Level 8", "Level 9", "Level 10")
    fun saveGameType(gameType: GameTypes) {
        val editor = sharedPreferences.edit()
        editor.putString("GameType", gameType.name)
        editor.apply()
    }

    fun getGameType(): GameTypes {
        if (sharedPreferences.getString("GameType", null) == null) {
            return GameTypes.COUNTRY
        } else if (sharedPreferences.getString("GameType", null) == "CAPITAL") {
            return GameTypes.CAPITAL
        } else if (sharedPreferences.getString("GameType", null) == "COUNTRY") {
            return GameTypes.COUNTRY
        } else if (sharedPreferences.getString("GameType", null) == "FLAG") {
            return GameTypes.FLAG
        } else {
            return GameTypes.COUNTRY
        }
    }

    fun setHighScore(score: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("HighScore", score)
        editor.apply()
    }

    fun getHighScore(): Int {
        return sharedPreferences.getInt("HighScore", 0)
    }


    fun setGameStatus(level: Int, score: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("level_$level", score)
        editor.apply()
    }

    fun getGameStatus(): List<GameLevel> {
        val gameLevels = mutableListOf<GameLevel>()
        for (i in 1..NUMBER_OF_LEVELS) {
            val score = sharedPreferences.getInt("level_$i", 0)
            var gameLevel: GameLevel
            if ((i>1) && (gameLevels[i-2].score > 0)) {
                gameLevel = GameLevel(i, GAME_LEVEL_NAMES[i-1], score, false, LevelStatus.ACTIVATED)
            } else if (score > 0) {
                gameLevel = GameLevel(i, GAME_LEVEL_NAMES[i-1], score, false, LevelStatus.ACTIVATED)
            } else {
                gameLevel = GameLevel(i, GAME_LEVEL_NAMES[i-1], score, false, LevelStatus.NOT_ACTIVATED)
            }
            gameLevels.add(gameLevel)
        }
        return gameLevels
    }
}