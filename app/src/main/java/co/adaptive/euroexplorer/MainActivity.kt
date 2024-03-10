package co.adaptive.euroexplorer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean



class MainActivity : AppCompatActivity() {

    private var interstitialAd: InterstitialAd? = null
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var adIsLoading: Boolean = false
    val AD_UNIT_ID = "ca-app-pub-4186914237786348~7546328190"
    private final val TAG = "MainActivity"
    private var gameCount = 0

    @SuppressLint("SetTextI18n")
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
        val buttonNewCapitals = findViewById<Button>(R.id.button_new_capitals)
        buttonNewCapitals.setOnClickListener {
            sharedPreferencesHelper.saveGameType(GameTypes.CAPITAL)
            loadMap()
        }
        val buttonNewCountries = findViewById<Button>(R.id.button_new_countries)
        buttonNewCountries.setOnClickListener {
            sharedPreferencesHelper.saveGameType(GameTypes.COUNTRY)
            loadMap()
        }
        val buttonNewFlags = findViewById<Button>(R.id.button_new_flags)
        buttonNewFlags.setOnClickListener {
            sharedPreferencesHelper.saveGameType(GameTypes.FLAG)
            loadMap()
        }

        updateHighScore()
        initializeMobileAdsSdk()
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("ABCDEF012345")).build()
        )
//        showInterstitial()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.fragments.firstOrNull()
        if (currentFragment is MapsFragment) {
            supportFragmentManager.beginTransaction().remove(currentFragment).commit()
            updateHighScore()
        } else {
            super.onBackPressed()
        }
    }

    private fun updateHighScore() {
        val sharedPreferencesHelper = SharedPreferencesHelper(baseContext)
        val highScore = sharedPreferencesHelper.getHighScore()
        if (highScore > 0) {
            val highScoreText = findViewById<TextView>(R.id.highscore)
            highScoreText.text = getString(R.string.high_score)+" $highScore"
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) { initializationStatus ->
            // Load an ad.
            //loadAd()
        }
    }

    private fun loadMap() {
        Log.d(TAG, "[loadMap]")
        if (gameCount > 0) {
//            loadAd()
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main, MapsFragment())
        transaction.commit()
        gameCount++
    }

    private fun loadAd() {
        Log.i(TAG, "[loadAd]")
        // Request a new ad if one isn't already loaded.
        if (adIsLoading || interstitialAd != null) {
            Log.d(TAG, "An ad is already loading, or one has already loaded.")
            return
        }
        adIsLoading = true

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    interstitialAd = null
                    adIsLoading = false
                    val error =
                        "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
                    Toast.makeText(
                        this@MainActivity,
                        "onAdFailedToLoad() with error $error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    interstitialAd = ad
                    adIsLoading = false
                    Toast.makeText(this@MainActivity, "onAdLoaded()", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }


}