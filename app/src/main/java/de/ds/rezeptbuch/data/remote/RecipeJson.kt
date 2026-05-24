package de.ds.rezeptbuch.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeJson(
    val id: Long? = null,
    val titel: String,
    val portionen: Int? = null,
    val arbeitszeit: Int? = null,
    val kochzeit: Int? = null,
    val zutaten: String? = null,
    val zubereitung: String? = null,
    val notizen: String? = null,
    @SerialName("bild_pfad")
    val bildPfad: String? = null,
    val quelle: String? = null,
    val bewertung: Int? = null,
    val kalorien: Int? = null,
    val kohlenhydrate: Float? = null,
    val eiweis: Float? = null,
    val fett: Float? = null,
    val kategorien: List<String> = emptyList()
)
