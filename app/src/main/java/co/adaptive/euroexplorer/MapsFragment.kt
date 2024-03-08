package co.adaptive.euroexplorer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
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
    private lateinit var question: TextView
    private lateinit var notification: TextView
    private lateinit var score: TextView
    private lateinit var airplane: ImageView
    private lateinit var gameLogic: Game
    private lateinit var googleMap: GoogleMap

    private val callback = OnMapReadyCallback { gM ->
        googleMap = gM
        val europe = LatLng(54.5260, 15.2551)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(europe,3.5f))
        googleMap.setOnMapClickListener(SingleTapMapClickListener { latLng ->
            val geocoder = context?.let { Geocoder(it, Locale("el")) }
            try {
                if (gameLogic.gameType == GameTypes.CAPITAL) {
                    val addresses = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 10)
                    addresses?.let {
                        val result = gameLogic.checkAnswers(it)
                        setAnswer(result.first, result.second)
                        updateScore()
                    } ?: run {
                        // Handle the case where addresses is null
                        Log.e("MapsFragment", "No addresses found")
                    }
                } else if (gameLogic.gameType == GameTypes.COUNTRY) {
                    val addresses = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    if (addresses.isNullOrEmpty()) {
                        Log.e("MapsFragment", "No addresses found")
                        return@SingleTapMapClickListener
                    } else {
                        val result = gameLogic.checkAnswer(addresses[0].countryName)
                        setAnswer(result.first, result.second)
                        updateScore()
                    }
                } else {
                    Log.e("MapsFragment", "Game type not supported")
                }
            } catch (e: Exception) {
                Log.e("MapsFragment", "Error getting location", e)
            }
            nextQuestion()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        gameLogic = Game(requireContext())
        question = view.findViewById(R.id.question)
        notification = view.findViewById(R.id.notification)
        score = view.findViewById(R.id.score)
        airplane = view.findViewById(R.id.airplane)
        question.text = gameLogic.getQuestion()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun loadQuestion() {
        question.text = gameLogic.getQuestion()
        hideNotification()
    }

    private fun hideNotification() {
        notification.text = ""
        notification.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        notification.visibility = View.INVISIBLE
    }

    private fun airplaneAnimation() {
        val slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in)
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

    private fun setAnswer(correct: Boolean, answer: String) {
        if (correct) {
            notification.text = getString(R.string.notification_success)
            airplaneAnimation()
//            notification.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.correct))
        } else {
            notification.text = getString(R.string.notification_wrong_answer)+ " " + answer
//            notification.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.incorrect))
        }
        notification.visibility = View.VISIBLE
    }

    private fun updateScore() {
        score.text = gameLogic.getScore().toString()
        animateScore()
    }

    private fun animateScore() {
        // Set up the animation
        val scoreAnimatorX: ObjectAnimator = ObjectAnimator.ofFloat<View>(
            score,
            View.SCALE_X,
            1f,
            2f
        )
        scoreAnimatorX.setDuration(1000) // Duration of animation in milliseconds

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
                reverseAnimator.setDuration(1000) // Duration of animation in milliseconds
                reverseAnimator.start()
            }
        })
        val scoreAnimatorY: ObjectAnimator = ObjectAnimator.ofFloat<View>(
            score,
            View.SCALE_Y,
            1f,2f
        )
        scoreAnimatorY.setDuration(1000) // Duration of animation in milliseconds

        // Add an animation listener to handle animation completion
        scoreAnimatorY.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // Reverse the animation
                val reverseAnimator: ObjectAnimator = ObjectAnimator.ofFloat<View>(
                    score,
                    View.SCALE_Y,
                    2f,1f
                )
                reverseAnimator.setDuration(1000) // Duration of animation in milliseconds
                reverseAnimator.start()
            }
        })
        // Start the animation
        scoreAnimatorX.start()
        scoreAnimatorY.start()
    }
    private fun nextQuestion() {
        try {
            question.text = gameLogic.getQuestion()
            if (gameLogic.gameType == GameTypes.CAPITAL) {
                val country = gameLogic.getCurrentCountry()
                if (country != null) {
                    val latLng = LatLng(country.coordinates.split(",")[0].toDouble(), country.coordinates.split(",")[1].toDouble())
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5.0f))
                }
            }
        } catch (e: Exception) {
            Log.e("MapsFragment", "Error getting next question", e)
        }

    }
}