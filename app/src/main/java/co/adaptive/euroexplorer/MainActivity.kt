package co.adaptive.euroexplorer

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPreferencesHelper = SharedPreferencesHelper(baseContext)
        val buttonNewGame = findViewById<Button>(R.id.button_new_game)
        buttonNewGame.setOnClickListener {
            //TODO: select game type
            sharedPreferencesHelper.saveGameType(GameTypes.CAPITAL)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.main, MapsFragment())
            transaction.commit()
        }
    }
}