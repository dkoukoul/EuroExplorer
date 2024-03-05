package co.adaptive.euroexplorer

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class MapsFragment : Fragment() {
    private lateinit var question: TextView
    private lateinit var notification: TextView
    private lateinit var gameLogic: Game
    private var gameType: GameTypes = GameTypes.CAPITAL
    private lateinit var googleMap: GoogleMap
//    private val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

    private val callback = OnMapReadyCallback { gM ->
        googleMap = gM
        val europe = LatLng(54.5260, 15.2551)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(europe,3.0f))
        googleMap.setOnMapClickListener { latLng ->
            val geocoder = context?.let { Geocoder(it, Locale("el")) }
            if (gameType == GameTypes.CAPITAL) {
                val addresses = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 6)
                addresses?.let {
                    val result = gameLogic.checkAnswers(it)
                    setAnswer(result)
                } ?: run {
                    // Handle the case where addresses is null
                    Log.e("MapsFragment", "No addresses found")
                }
            } else {
                val addresses = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)
                setAnswer(gameLogic.checkAnswer(addresses!![0].countryName))
            }

            nextQuestion()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        gameLogic = Game(requireContext())
//        gameType = sharedPreferencesHelper.getGameType()!!
        question = view.findViewById(R.id.question)
        notification = view.findViewById(R.id.notification)

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

    private fun setAnswer(correct: Boolean) {
        if (correct) {
            notification.text = "Correct! Your score is ${gameLogic.getScore()}"
            notification.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.correct))
        } else {
            notification.text = "Incorrect! Your score is ${gameLogic.getScore()}"
            notification.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.incorrect))
        }
        notification.visibility = View.VISIBLE
    }

    private fun nextQuestion() {
        try {
            question.text = gameLogic.getQuestion()
            if (gameType == GameTypes.CAPITAL) {
                val country = gameLogic.getCurrentCountry()
                if (country != null) {
                    val latLng = LatLng(country.coordinates.split(",")[0].toDouble(), country.coordinates.split(",")[1].toDouble())
                    val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                    mapFragment?.getMapAsync { googleMap ->
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5.0f))
                    }
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5.0f))
                }
            }
        } catch (e: Exception) {
            Log.e("MapsFragment", "Error getting next question", e)
        }

    }
}