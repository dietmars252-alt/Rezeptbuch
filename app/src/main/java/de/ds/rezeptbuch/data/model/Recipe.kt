package de.ds.rezeptbuch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rezepte")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titel: String,
    val kategorien: List<String>, // Hier auf List<String> geändert
    val portionen: Int,
    val anweisungen: List<String>,
    val bewertung: Int = 0,
    val bildUrl: String? = null
)