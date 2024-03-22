package co.adaptive.euroexplorer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class SingleTapMapClickListener(private val actualListener: GoogleMap.OnMapClickListener) : GoogleMap.OnMapClickListener {
    private var lastClickTime: Long = 0

    override fun onMapClick(point: LatLng) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > 1000) { // 1000 ms = 1 s
            lastClickTime = currentTime
            actualListener.onMapClick(point)
        }
    }
}

class MapsFragment : Fragment() {
    private val TAG = "MapsFragment"
    private val NOTIFICATION_DURATION = 5000L
    private val CLOUD_ANIMATION_DURATION = 1000L
    private val SCORE_ANIMATION_DURATION = 1000L
    private val AIRPLANE_ANIMATION_DURATION = 1500L

    private lateinit var question: TextView
    private lateinit var notification: TextView
    private lateinit var score: TextView
    private lateinit var gameOverText: TextView
    private lateinit var gameOverLayout: LinearLayout
    private lateinit var gameOverButton: Button
    private lateinit var airplane: ImageView
    private lateinit var gameLogic: Game
    private lateinit var topBar: LinearLayout
    private lateinit var googleMap: GoogleMap
    private lateinit var flag: ImageView
    private lateinit var cloudsContainer: View
    private lateinit var cloudImage: ImageView

    private val callback = OnMapReadyCallback { gM ->
        googleMap = gM
        //googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
        val europe = LatLng(54.5260, 15.2551)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(europe,4.0f))
        googleMap.setOnMapClickListener(SingleTapMapClickListener { latLng ->
            val geocoder = context?.let { Geocoder(it, Locale("el")) }
            try {
                Log.i(TAG, "[onMapClick] ${gameLogic.gameType}")
                val addresses = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (gameLogic.gameType == GameTypes.CAPITAL) {
                    val result = addresses?.get(0)?.let { gameLogic.checkAnswerCapital(it) }
                    if (result != null) {
                        setAnswer(result.first, result.second)
                        updateScore()
                    }
                } else if ((gameLogic.gameType == GameTypes.COUNTRY) || (gameLogic.gameType == GameTypes.FLAG)){
                    val result = addresses?.get(0)?.let { gameLogic.checkAnswer(it.countryName) }
                    if (result != null) {
                        setAnswer(result.first, result.second)
                    }
                    updateScore()
                } else {
                    Log.e(TAG, "[onMapClick] Game type not supported")
                }
            } catch (e: Exception) {
                Log.e(TAG, "[onMapClick] Error getting location", e)
            }
            nextQuestion()
        })
        // Start the game
        nextQuestion()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        gameLogic = Game(requireContext())
        topBar = view.findViewById(R.id.topbar)
        question = view.findViewById(R.id.question)
        notification = view.findViewById(R.id.notification)
        score = view.findViewById(R.id.score)
        gameOverText = view.findViewById(R.id.gameovertext)
        gameOverButton = view.findViewById(R.id.gameoverbutton)
        airplane = view.findViewById(R.id.airplane)
        flag = view.findViewById(R.id.flagImage)
        cloudsContainer = view.findViewById(R.id.cloudsContainer)
        gameOverLayout = view.findViewById(R.id.gameoverlayout)
        gameOverLayout.visibility = View.GONE
        flag.visibility = View.GONE
        question.visibility = View.VISIBLE
        topBar.visibility = View.VISIBLE
        cloudsContainer.visibility = View.VISIBLE
        cloudImage = view.findViewById(R.id.cloudsImage)
        cloudImage.visibility = View.VISIBLE

        gameOverButton.setOnClickListener {
            activity?.onBackPressed()
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun hideNotification() {
        notification.text = ""
        notification.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        notification.visibility = View.INVISIBLE
    }


    private fun setAnswer(correct: Boolean, answer: String) {
        Log.i(TAG, "[setAnswer] $correct $answer")
        if (correct) {
            notification.text = getString(R.string.notification_success)
            airplaneAnimation()
        } else {
            notification.text = getString(R.string.notification_wrong_answer)+ " " + answer
        }

        notification.visibility = View.VISIBLE
        // Clear the notification text after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            notification.text = ""
        }, NOTIFICATION_DURATION)
    }

    private fun updateScore() {
        Log.i(TAG, "[updateScore]")
        score.text = gameLogic.getScore().toString()
        fadeOutCloud()
        animateScore()
    }

    @SuppressLint("SetTextI18n")
    private fun setGameOver(score: Int) {
        Log.i(TAG, "[setGameOver] $score")
        val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        topBar.visibility = View.INVISIBLE
        cloudsContainer.visibility = View.INVISIBLE
        gameOverLayout.visibility = View.VISIBLE
        val prevHighScore = sharedPreferencesHelper.getHighScore()
        if (score > prevHighScore) {
            gameOverText.text = getString(R.string.game_over_high_score) + " " + score
            sharedPreferencesHelper.setHighScore(score)
        } else {
            gameOverText.text = getString(R.string.game_over) + " " + score
        }
        gameLogic.resetGame()
    }

    private fun nextQuestion() {
        Log.i(TAG, "[nextQuestion] ${gameLogic.gameType}")
        try {
            if (gameLogic.getNumOfQuestions() > 9) {
                Log.i(TAG, "Game over")
                setGameOver(gameLogic.getScore())
                return
            }
            question.text = gameLogic.getQuestion()
            question.gravity = Gravity.CENTER //For other that non flag games
            if (gameLogic.gameType == GameTypes.CAPITAL) {
                val country = gameLogic.getCurrentCountry()
                val latLng = LatLng(
                    country.coordinates.split(",")[0].toDouble(),
                    country.coordinates.split(",")[1].toDouble()
                )
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5.0f))
            } else if (gameLogic.gameType == GameTypes.FLAG) {
                val drawableId = gameLogic.getCurrentFlag()
                val drawableFlag = ContextCompat.getDrawable(requireContext(), drawableId)
                flag.setImageDrawable(drawableFlag)
                flag.visibility = View.VISIBLE
                question.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                question.visibility = View.INVISIBLE
                cloudImage.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting next question", e)
        }
        fadeInCloud()
    }
    private fun fadeInCloud() {
        Log.i(TAG, "[fadeInCloud]")
        try {
            // Create the fade in animation
            val fadeIn = ObjectAnimator.ofFloat(cloudsContainer, "alpha", 0f, 1f)
            fadeIn.duration = CLOUD_ANIMATION_DURATION/2
            fadeIn.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error animating clouds", e)
        }
    }
    private fun fadeOutCloud() {
        Log.i(TAG, "[fadeOutCloud]")
        try {
            val fadeOut = ObjectAnimator.ofFloat(cloudsContainer, "alpha", 1f, 0f)
            fadeOut.duration = CLOUD_ANIMATION_DURATION/2
            fadeOut.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error animating clouds", e)
        }
    }
    private fun animateScore() {
        Log.i(TAG, "[animateScore]")
        try {
            val scoreAnimatorX: ObjectAnimator = ObjectAnimator.ofFloat<View>(
                score,
                View.SCALE_X,
                1f, 2f
            )
            scoreAnimatorX.setDuration(SCORE_ANIMATION_DURATION) // Duration of animation in milliseconds
            // Add an animation listener to handle animation completion
            scoreAnimatorX.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    // Reverse the animation
                    val reverseAnimator: ObjectAnimator = ObjectAnimator.ofFloat<View>(
                        score,
                        View.SCALE_X,
                        2f,
                        1f
                    )
                    reverseAnimator.setDuration(SCORE_ANIMATION_DURATION) // Duration of animation in milliseconds
                    reverseAnimator.start()
                }
            })
            val scoreAnimatorY: ObjectAnimator = ObjectAnimator.ofFloat<View>(
                score,
                View.SCALE_Y,
                1f, 2f
            )
            scoreAnimatorY.setDuration(SCORE_ANIMATION_DURATION) // Duration of animation in milliseconds

            // Add an animation listener to handle animation completion
            scoreAnimatorY.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    // Reverse the animation
                    val reverseAnimator: ObjectAnimator = ObjectAnimator.ofFloat<View>(
                        score,
                        View.SCALE_Y,
                        2f, 1f
                    )
                    reverseAnimator.setDuration(SCORE_ANIMATION_DURATION) // Duration of animation in milliseconds
                    reverseAnimator.start()
                }
            })
            // Start the animation
            scoreAnimatorX.start()
            scoreAnimatorY.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error animating score", e)
        }
    }

    private fun airplaneAnimation() {
        val slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in)
        slideInAnimation.duration = AIRPLANE_ANIMATION_DURATION
        slideInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                airplane.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {
                airplane.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {
                // Do nothing
            }
        })

        airplane.startAnimation(slideInAnimation)
    }
}