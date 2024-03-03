package co.adaptive.euroexplorer.dto

import kotlinx.serialization.Serializable

@Serializable
class Country : ArrayList<CountriesItem>()

@Serializable
data class CountriesItem(
    val area: Int,
    val capital: String,
    val countryCode: String,
    val density: Double,
    val language: List<String>,
    val name: String,
    val population: Int,
    val article: String,
    val eu: Boolean
)