package co.adaptive.euroexplorer.dto

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    val name: String,
    val coordinates: String,
    val area: Double,
    val capital: String,
    val countryCode: String,
    val density: Double,
    val language: List<String>,
    val population: Int,
    val article: String,
    val eu: Boolean
)