package de.ds.rezeptbuch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kategorien")
data class Category(
    @PrimaryKey val name: String
)
