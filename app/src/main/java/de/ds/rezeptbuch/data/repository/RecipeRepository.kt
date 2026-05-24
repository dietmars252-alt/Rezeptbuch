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


    suspend fun deleteRecipe(recipeId: Long) {
        recipeDao.deleteRecipe(recipeId)
    }

    suspend fun insertCategories(categories: List<Category>) {
        recipeDao.insertCategories(categories)
    }

    suspend fun getRecipeByTitle(title: String): RecipeWithIngredients? {
        return recipeDao.getRecipeByTitle(title)
    }
}