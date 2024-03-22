package co.adaptive.euroexplorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class GameLevelsFragment : Fragment() {

    private lateinit var prefs : SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SharedPreferencesHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_levels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TEST
        prefs.setGameStatus(1,27)
        prefs.setGameStatus(2,20)
        prefs.setGameStatus(3,15)
        val gameLevels = prefs.getGameStatus()
        val adapter = GameLevelsAdapter(gameLevels,requireActivity() as MainActivity)
        val recyclerView = view.findViewById<RecyclerView>(R.id.game_levels_recycler_view)
        recyclerView.adapter = adapter
    }
}