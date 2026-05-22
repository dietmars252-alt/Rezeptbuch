package de.ds.rezeptbuch.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "zutaten",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["rezeptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rezeptId")]
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rezeptId: Long,
    val name: String,
    val menge: Double,
    val einheit: String
)
