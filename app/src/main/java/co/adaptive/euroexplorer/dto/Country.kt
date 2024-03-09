package co.adaptive.euroexplorer.dto

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    val name: String,
    val coordinates: String,
    val area: Double,
    val capital: Capital,
    val countryCode: String,
    val density: Double,
    val language: List<String>,
    val population: Int,
    val article: String,
    val eu: Boolean
)

@Serializable
data class Capital(
    val name_en: String,
    val name_el: String,
    val coordinates: String
)