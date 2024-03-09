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
    private lateinit var flag: ImageView
    private lateinit var cloudsContainer: View

    private val callback = OnMapReadyCallback { gM ->
        googleMap = gM
        //googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
        val europe = LatLng(54.5260, 15.2551)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(europe,3.5f))
        googleMap.setOnMapClickListener(SingleTapMapClickListener { latLng ->
            val geocoder = context?.let { Geocoder(it) }
            try {
                Log.i("MapsFragment", "[onMapClick] ${gameLogic.gameType}")
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
                    Log.e("MapsFragment", "[onMapClick] Game type not supported")
                }
            } catch (e: Exception) {
                Log.e("MapsFragment", "[onMapClick] Error getting location", e)
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
        flag = view.findViewById(R.id.flagImage)
        cloudsContainer = view.findViewById(R.id.cloudsContainer)
        flag.visibility = View.INVISIBLE
        nextQuestion()
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
        Log.i("MapsFragment", "[setAnswer] $correct $answer")
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
        Log.i("MapsFragment", "[updateScore]")
        score.text = gameLogic.getScore().toString()
        animateCloud()
        animateScore()
    }


    private fun nextQuestion() {
        Log.i("MapsFragment", "[nextQuestion] ${gameLogic.gameType}")
        try {
            question.text = gameLogic.getQuestion()
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
            } else {
                Log.e("MapsFragment", "Game type not supported")
            }
        } catch (e: Exception) {
            Log.e("MapsFragment", "Error getting next question", e)
        }

    }

    private fun animateCloud() {
        Log.i("MapsFragment", "[animateCloud]")
        try {
            // Create the fade out animation
            val fadeOut = ObjectAnimator.ofFloat(cloudsContainer, "alpha", 1f, 0f)
            fadeOut.duration = 500 // Duration in milliseconds

            // Create the fade in animation
            val fadeIn = ObjectAnimator.ofFloat(cloudsContainer, "alpha", 0f, 1f)
            fadeIn.duration = 500 // Duration in milliseconds

            // Add an animation listener to the fade out animation to start the fade in animation when the fade out animation ends
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fadeIn.start()
                }
            })

            // Start the fade out animation
            fadeOut.start()
        } catch (e: Exception) {
            Log.e("MapsFragment", "Error animating clouds", e)
        }
    }
    private fun animateScore() {
        Log.i("MapsFragment", "[animateScore]")
        try {


            // Set up the animation
            val scoreAnimatorX: ObjectAnimator = ObjectAnimator.ofFloat<View>(
                score,
                View.SCALE_X,
                1f, 2f
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
                1f, 2f
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
                        2f, 1f
                    )
                    reverseAnimator.setDuration(1000) // Duration of animation in milliseconds
                    reverseAnimator.start()
                }
            })
            // Start the animation
            scoreAnimatorX.start()
            scoreAnimatorY.start()
        } catch (e: Exception) {
            Log.e("MapsFragment", "Error animating score", e)
        }
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
}