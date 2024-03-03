package co.adaptive.euroexplorer

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class MapsFragment : Fragment() {
    private lateinit var question: TextView
    private lateinit var notification: TextView
    private lateinit var gameLogic: Game

    private val callback = OnMapReadyCallback { googleMap ->
        val europe = LatLng(54.5260, 15.2551)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(europe,3.0f))
        googleMap.setOnMapClickListener { latLng ->
            val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
            val addresses = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)
            setAnswer(gameLogic.checkAnswer(addresses!![0].countryName))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        gameLogic = Game(requireContext())
        gameLogic.loadQuestions()
        question = view.findViewById(R.id.question)
        notification = view.findViewById(R.id.notification)
        loadQuestion()
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


}