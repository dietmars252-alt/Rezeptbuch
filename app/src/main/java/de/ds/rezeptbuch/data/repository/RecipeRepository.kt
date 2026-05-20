package de.ds.rezeptbuch.data.repository

import de.ds.rezeptbuch.data.local.RecipeDao
import de.ds.rezeptbuch.data.model.Category
import de.ds.rezeptbuch.data.model.Ingredient
import de.ds.rezeptbuch.data.model.Recipe
import de.ds.rezeptbuch.data.model.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {

    val allRecipes: Flow<List<RecipeWithIngredients>> = recipeDao.getAllRecipes()
    val allCategories: Flow<List<Category>> = recipeDao.getAllCategories()
    val favoriteRecipes: Flow<List<RecipeWithIngredients>> = recipeDao.getFavoriteRecipes()

    fun getRecipeById(id: Long): Flow<RecipeWithIngredients?> {
        return recipeDao.getRecipeById(id)
    }

    // WICHTIG: Die Funktionsweise im DAO muss eventuell angepasst werden,
    // da wir jetzt nach einem String innerhalb eines Text-Blocks (JSON) suchen.
    // Typischerweise nutzt man im Dao dafür: @Query("SELECT * FROM rezepte WHERE kategorien LIKE '%' || :category || '%'")
    fun getRecipesByCategory(category: String): Flow<List<RecipeWithIngredients>> {
        return recipeDao.getRecipesByCategory(category)
    }

    suspend fun insertRecipeWithIngredients(recipe: Recipe, ingredients: List<Ingredient>) {
        val recipeId = recipeDao.insertRecipe(recipe)
        val ingredientsWithId = ingredients.map { it.copy(rezeptId = recipeId) }
        recipeDao.insertIngredients(ingredientsWithId)
    }

    suspend fun updateRecipeWithIngredients(recipe: Recipe, ingredients: List<Ingredient>) {
        recipeDao.updateRecipe(recipe)
        recipeDao.deleteIngredientsByRecipeId(recipe.id)
        val ingredientsWithId = ingredients.map { it.copy(rezeptId = recipe.id) }
        recipeDao.insertIngredients(ingredientsWithId)
    }

    suspend fun updateFavoriteStatus(recipeId: Long, isFavorite: Boolean) {
        recipeDao.updateFavoriteStatus(recipeId, isFavorite)
    }

    suspend fun deleteRecipe(recipeId: Long) {
        recipeDao.deleteRecipe(recipeId)
    }

    suspend fun seedMockData() {
        val categories = listOf(
            Category("Backwaren"),
            Category("Fleischgerichte"),
            Category("Pasta"),
            Category("Vegetarisch"),
            Category("Dessert"),
            Category("Suppen"),
            Category("Schnelle Küche") // Neue Testkategorie
        )
        recipeDao.insertCategories(categories)

        val mockRecipes = listOf(
            Pair(
                // Jetzt mit "kategorien = listOf(...)" statt einem einzelnen String
                Recipe(
                    titel = "Apfelkuchen mit Streuseln",
                    kategorien = listOf("Backwaren", "Vegetarisch", "Dessert"),
                    portionen = 12,
                    anweisungen = listOf(
                        "Teig aus Mehl, Butter und Zucker kneten.",
                        "Äpfel schälen und schneiden.",
                        "Kuchen mit Streuseln bedecken und bei 180°C backen."
                    )
                ),
                listOf(
                    Ingredient(name = "Äpfel", menge = 1.0, einheit = "kg", rezeptId = 0),
                    Ingredient(name = "Mehl", menge = 500.0, einheit = "g", rezeptId = 0),
                    Ingredient(name = "Butter", menge = 250.0, einheit = "g", rezeptId = 0),
                    Ingredient(name = "Zucker", menge = 200.0, einheit = "g", rezeptId = 0)
                )
            ),
            Pair(
                Recipe(
                    titel = "Chili con Carne aus dem Ofen",
                    kategorien = listOf("Fleischgerichte"),
                    portionen = 4,
                    anweisungen = listOf(
                        "Fleisch anbraten.",
                        "Mit Bohnen und Tomaten in eine Auflaufform geben.",
                        "Im Ofen ca. 30 Minuten garen."
                    )
                ),
                listOf(
                    Ingredient(name = "Hackfleisch", menge = 500.0, einheit = "g", rezeptId = 0),
                    Ingredient(name = "Kidneybohnen", menge = 1.0, einheit = "Dose", rezeptId = 0),
                    Ingredient(name = "Tomaten passier", menge = 400.0, einheit = "ml", rezeptId = 0)
                )
            ),
            Pair(
                Recipe(
                    titel = "Gnocchi mit Bratwurstklößchen",
                    kategorien = listOf("Pasta", "Schnelle Küche"),
                    portionen = 2,
                    anweisungen = listOf(
                        "Bratwurstbrät als kleine Klößchen aus der Haut drücken.",
                        "Klößchen und Gnocchi goldbraun anbraten."
                    )
                ),
                listOf(
                    Ingredient(name = "Gnocchi", menge = 500.0, einheit = "g", rezeptId = 0),
                    Ingredient(name = "Bratwurst", menge = 2.0, einheit = "Stück", rezeptId = 0),
                    Ingredient(name = "Salbei", menge = 1.0, einheit = "Bund", rezeptId = 0)
                )
            )
        )

        mockRecipes.forEach { (recipe, ingredients) ->
            insertRecipeWithIngredients(recipe, ingredients)
        }
    }
}