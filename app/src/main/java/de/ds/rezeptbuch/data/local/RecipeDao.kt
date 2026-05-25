package de.ds.rezeptbuch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.ds.rezeptbuch.data.model.Category
import de.ds.rezeptbuch.data.model.Ingredient
import de.ds.rezeptbuch.data.model.Recipe
import de.ds.rezeptbuch.data.model.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM rezepte")
    fun getAllRecipes(): Flow<List<RecipeWithIngredients>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Query("DELETE FROM zutaten WHERE rezeptId = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRecipe(recipe: Recipe)

    @Query("DELETE FROM rezepte WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: Long)

    // Category operations
    @Query("SELECT * FROM kategorien")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Query("DELETE FROM kategorien WHERE name = :name")
    suspend fun deleteCategoryByName(name: String)

    @Transaction
    @Query("SELECT * FROM rezepte")
    suspend fun getAllRecipesOnce(): List<RecipeWithIngredients>

    @Transaction
    @Query("SELECT * FROM rezepte WHERE titel = :title LIMIT 1")
    suspend fun getRecipeByTitle(title: String): RecipeWithIngredients?
}