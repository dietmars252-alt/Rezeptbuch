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

    @Transaction
    @Query("SELECT * FROM rezepte WHERE id = :recipeId")
    fun getRecipeById(recipeId: Long): Flow<RecipeWithIngredients?>

    // ANGEPASST: Nutzt jetzt 'kategorien' und sucht per LIKE-Operator nach dem String im JSON-Array
    @Transaction
    @Query("SELECT * FROM rezepte WHERE kategorien LIKE '%' || :categoryName || '%'")
    fun getRecipesByCategory(categoryName: String): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM rezepte WHERE bewertung >= 4")
    fun getTopRatedRecipes(): Flow<List<RecipeWithIngredients>>

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

    @Query("UPDATE rezepte SET bewertung = :stars WHERE id = :recipeId")
    suspend fun updateRecipeRating(recipeId: Long, stars: Int)

    // Category operations
    @Query("SELECT * FROM kategorien")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)
}