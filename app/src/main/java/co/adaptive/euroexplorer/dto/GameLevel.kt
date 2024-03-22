package co.adaptive.euroexplorer.dto

data class GameLevel(
    val level: Int,
    val name: String,
    val score: Int,
    val isSelectable: Boolean,
    val status: LevelStatus
)

enum class LevelStatus {
    NOT_ACTIVATED,
    ACTIVATED,
    GOLD,
    SILVER,
    BRONZE
}