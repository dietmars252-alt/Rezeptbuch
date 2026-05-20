package de.ds.rezeptbuch.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "rezeptId"
    )
    val ingredients: List<Ingredient>
)
