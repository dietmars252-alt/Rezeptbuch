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

    // Geändert von favoriteRecipes zu topRatedRecipes
    val topRatedRecipes: Flow<List<RecipeWithIngredients>> = recipeDao.getTopRatedRecipes()

    fun getRecipeById(id: Long): Flow<RecipeWithIngredients?> {
        return recipeDao.getRecipeById(id)
    }

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

    // NEU: Ersetzt updateFavoriteStatus
    suspend fun updateRecipeRating(recipeId: Long, stars: Int) {
        // Sicherstellen, dass die Sterne im Bereich von 0 bis 5 liegen
        val sanitizedStars = stars.coerceIn(0, 5)
        recipeDao.updateRecipeRating(recipeId, sanitizedStars)
    }

    suspend fun deleteRecipe(recipeId: Long) {
        recipeDao.deleteRecipe(recipeId)
    }

    suspend fun insertCategories(categories: List<Category>) {
        recipeDao.insertCategories(categories)
    }

    suspend fun seedMockData() {
        val categories = listOf(
            Category("Backwaren"), Category("Fleischgerichte"), Category("Pasta"),
            Category("Vegetarisch"), Category("Dessert"), Category("Suppen")
        )
        recipeDao.insertCategories(categories)

        // HIER WURDEN DIE REZEPTE ANGEPASST (bewertung = X statt istFavorit)
        val mockRecipes = listOf(
            Pair(
                Recipe(
                    titel = "Apfelkuchen mit Streuseln",
                    kategorien = listOf("Backwaren"),
                    portionen = 12,
                    bewertung = 5,
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
                    bewertung = 4,
                    anweisungen = listOf(
                        "Fleisch anbraten.",
                        "Mit Bohnen und Tomaten in eine Auflaufform geben.",
                        "Im Ofen ca. 30 Minuten garen."
                    )
                ),
                listOf(
                    Ingredient(name = "Hackfleisch", menge = 500.0, einheit = "g", rezeptId = 0),
                    Ingredient(name = "Kidneybohnen", menge = 1.0, einheit = "Dose", rezeptId = 0),
                    Ingredient(
                        name = "Tomaten passier",
                        menge = 400.0,
                        einheit = "ml",
                        rezeptId = 0
                    )
                )
            ),
            Pair(
                Recipe(
                    titel = "Gnocchi mit Bratwurstklößchen",
                    kategorien = listOf("Pasta"),
                    portionen = 2,
                    bewertung = 3,
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
            ),

            Pair(
                    Recipe(
                        titel = "Auflauf mit Schweinefilet",
                        kategorien = listOf("Aufläufe", "Fleischgerichte"),
                        portionen = 4,
                        bewertung = 0,
                        arbeitszeit = 45,
                        kochzeit = 55,
                        notizen = "",
                        bildpfad = "uploads/auflauf_mit_schweinefilet_6936f22f9a73a.webp",
                        quelle = "",
                        kalorien = null,
                        kohlenhydrate = null,
                        eiweis = null,
                        fett = null,
                        anweisungen = listOf(
                            "Das Fleisch etwa 1 Stunde vor der Zubereitung aus dem Kühlschrank nehmen, damit es auf Zimmertemperatur kommt. Die Kartoffeln waschen und in Salzwasser je nach Größe 20–25 Minuten garen. Den Backofen auf 190 °C Ober- und Unterhitze vorheizen. Eine Auflaufform einfetten.",
                            "Das Schweinefilet trocken tupfen und in etwa 2 cm dicke Scheiben schneiden. Die Zwiebeln schälen und fein würfeln. Die Champignons putzen und in nicht zu dünne Scheiben schneiden.",
                            "Das Öl in einer Pfanne erhitzen und die Schweinefiletmedaillons darin von beiden Seiten je 1 Minute scharf anbraten. Herausnehmen, in der Auflaufform verteilen und mit Salz und Pfeffer würzen."
                        )

            ),

                        listOf(
                            Ingredient(name = "Schweinefilet", menge = 800.0, einheit = "g", rezeptId = 3),
                            Ingredient(name = "Kartoffeln (festkochend)", menge = 800.0, einheit = "g", rezeptId = 3),
                            Ingredient(name = "Champignons (frisch)", menge = 250.0, einheit = "g", rezeptId = 3),
                            Ingredient(name = "Gemüsebrühe", menge = 300.0, einheit = "ml", rezeptId = 3),
                            Ingredient(name = "Sahne", menge = 250.0, einheit = "ml", rezeptId = 3),
                            Ingredient(name = "Saure Sahne", menge = 200.0, einheit = "g", rezeptId = 3),
                            Ingredient(name = "Käse (gerieben)", menge = 200.0, einheit = "g", rezeptId = 3),
                            Ingredient(name = "Zwiebeln", menge = 2.0, einheit = "Stück", rezeptId = 3),
                            Ingredient(name = "Bratöl", menge = 2.0, einheit = "EL", rezeptId = 3),
                            Ingredient(name = "Salz, Pfeffer", menge = 1.0, einheit = "Prise", rezeptId = 3),
                            Ingredient(name = "Currypulver", menge = 1.0, einheit = "Prise", rezeptId = 3),
                            Ingredient(name = "Fett für die Auflaufform", menge = 1.0, einheit = "etwas", rezeptId = 3)
                    )

            )
        )




        mockRecipes.forEach { (recipe, ingredients) ->
            insertRecipeWithIngredients(recipe, ingredients)
        }
    }
}