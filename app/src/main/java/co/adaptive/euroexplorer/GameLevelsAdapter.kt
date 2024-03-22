package co.adaptive.euroexplorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.adaptive.euroexplorer.dto.GameLevel
import co.adaptive.euroexplorer.dto.LevelStatus

class GameLevelsAdapter(private val gameLevels: List<GameLevel>, private val listener: OnLevelClickListener) : RecyclerView.Adapter<GameLevelsAdapter.GameLevelViewHolder>() {
    interface OnLevelClickListener {
        fun onLevelClick(level: GameLevel)
    }
    class GameLevelViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameLevelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_level_item, parent, false)
        return GameLevelViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameLevelViewHolder, position: Int) {
        val gameLevel = gameLevels[position]
        val circleImageButton = holder.view.findViewById<ImageButton>(R.id.circle_image_button)
        val levelNameTextView = holder.view.findViewById<TextView>(R.id.level_name_text_view)

        levelNameTextView.text = gameLevel.name
        when {
            gameLevel.score > 25 -> {
                circleImageButton.setBackgroundResource(R.drawable.medal_gold)
            }
            gameLevel.score > 18 -> {
                circleImageButton.setBackgroundResource(R.drawable.medal_silver)
            }
            gameLevel.score > 0 -> {
                circleImageButton.setBackgroundResource(R.drawable.medal_bronze)
            }
            else -> {
                if (gameLevel.status == LevelStatus.ACTIVATED) {
                    circleImageButton.setBackgroundResource(R.drawable.travel)
                    circleImageButton.backgroundTintList = ContextCompat.getColorStateList(holder.view.context, R.color.primary)
                } else {
                    circleImageButton.setBackgroundResource(R.drawable.level_icon)
                    circleImageButton.backgroundTintList = null
                }
            }
        }

        circleImageButton.setOnClickListener {
            if (gameLevel.status == LevelStatus.ACTIVATED) {
                listener.onLevelClick(gameLevel)
            } else {
                //Notify user that level is not activated with a toast
                Toast.makeText(holder.view.context, "Level is not activated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = gameLevels.size
}