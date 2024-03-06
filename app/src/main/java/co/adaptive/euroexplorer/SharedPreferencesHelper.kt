package co.adaptive.euroexplorer

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("co.adaptive.euroexplorer", Context.MODE_PRIVATE)

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

    fun saveApplicationStatus(status: String) {
        val editor = sharedPreferences.edit()
        editor.putString("GameStatus", status)
        editor.apply()
    }

    fun getApplicationStatus(): String? {
        return sharedPreferences.getString("GameStatus", null)
    }
}