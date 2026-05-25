package de.ds.rezeptbuch.data.remote

import de.ds.rezeptbuch.data.model.Category
import de.ds.rezeptbuch.data.model.Ingredient
import de.ds.rezeptbuch.data.model.Recipe
import de.ds.rezeptbuch.data.model.RecipeWithIngredients
import de.ds.rezeptbuch.data.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.util.regex.Pattern

class JsonImportService(private val repository: RecipeRepository) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        prettyPrint = true
    }

    suspend fun exportJson(): String {
        val recipesWithIngredients = repository.allRecipes.first()
        val recipesJson = recipesWithIngredients.map { rwi ->
            val r = rwi.recipe
            RecipeJson(
                id = r.id,
                titel = r.titel,
                portionen = r.portionen,
                arbeitszeit = r.arbeitszeit,
                kochzeit = r.kochzeit,
                zutaten = formatIngredientsForExport(rwi.ingredients),
                zubereitung = r.anweisungen.joinToString("\r\n"),
                notizen = r.notizen,
                bildPfad = r.bildpfad,
                quelle = r.quelle,
                bewertung = r.bewertung,
                kalorien = r.kalorien,
                kohlenhydrate = r.kohlenhydrate,
                eiweis = r.eiweis,
                fett = r.fett,
                kategorien = r.kategorien
            )
        }
        return json.encodeToString(recipesJson)
    }

    private fun formatIngredientsForExport(ingredients: List<Ingredient>): String {
        return ingredients.joinToString("\r\n") { ing ->
            val mengeText = if (ing.menge > 0) "${formatDouble(ing.menge)} " else ""
            val einheitText = if (ing.einheit.isNotBlank()) "${ing.einheit} " else ""
            "$mengeText$einheitText${ing.name}".trim()
        }
    }

    private fun formatDouble(d: Double): String {
        return if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString().replace(".", ",")
    }

    suspend fun importJson(jsonString: String) {
        try {
            val existingRecipes = repository.allRecipes.first()
            val recipesJson = json.decodeFromString<List<RecipeJson>>(jsonString)
            
            // Neue Kategorien sammeln
            val allNewCategoryNames = recipesJson.flatMap { it.kategorien }.toSet()
            if (allNewCategoryNames.isNotEmpty()) {
                repository.insertCategories(allNewCategoryNames.map { Category(it) })
            }

            recipesJson.forEach { rJson ->
                val newAnweisungen = parseInstructions(rJson.zubereitung)
                val newIngredients = parseIngredients(rJson.zutaten)
                val formattedBildPfad = formatBildPfad(rJson.bildPfad)

                val existing = existingRecipes.find { it.recipe.titel.trim().equals(rJson.titel.trim(), ignoreCase = true) }

                if (existing == null) {
                    // Neues Rezept importieren
                    val recipe = createRecipeFrom(rJson, newAnweisungen, formattedBildPfad)
                    repository.insertRecipeWithIngredients(recipe, newIngredients)
                } else if (hasChanges(existing, rJson, newAnweisungen, newIngredients, formattedBildPfad)) {
                    // Geändertes Rezept aktualisieren
                    val updatedRecipe = createRecipeFrom(rJson, newAnweisungen, formattedBildPfad).copy(id = existing.recipe.id)
                    repository.updateRecipeWithIngredients(updatedRecipe, newIngredients)
                }
                // Ansonsten: Überspringen, da identisch
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createRecipeFrom(rJson: RecipeJson, anweisungen: List<String>, bildpfad: String?): Recipe {
        return Recipe(
            titel = rJson.titel,
            kategorien = rJson.kategorien,
            portionen = rJson.portionen ?: 1,
            anweisungen = anweisungen,
            bewertung = rJson.bewertung ?: 0,
            arbeitszeit = rJson.arbeitszeit,
            kochzeit = rJson.kochzeit,
            notizen = rJson.notizen ?: "",
            bildpfad = bildpfad,
            quelle = rJson.quelle ?: "",
            kalorien = rJson.kalorien,
            kohlenhydrate = rJson.kohlenhydrate,
            eiweis = rJson.eiweis,
            fett = rJson.fett
        )
    }

    private fun hasChanges(
        existing: RecipeWithIngredients,
        rJson: RecipeJson,
        newAnweisungen: List<String>,
        newIngredients: List<Ingredient>,
        newBildPfad: String?
    ): Boolean {
        val r = existing.recipe
        
        // Einfache Felder prüfen
        if (r.portionen != (rJson.portionen ?: 1)) return true
        if (r.arbeitszeit != rJson.arbeitszeit) return true
        if (r.kochzeit != rJson.kochzeit) return true
        if (r.notizen != (rJson.notizen ?: "")) return true
        if (r.quelle != (rJson.quelle ?: "")) return true
        if (r.bildpfad != newBildPfad) return true
        if (r.kalorien != rJson.kalorien) return true
        if (r.fett != rJson.fett) return true
        if (r.eiweis != rJson.eiweis) return true
        if (r.kohlenhydrate != rJson.kohlenhydrate) return true
        
        // Listen prüfen
        if (r.kategorien != rJson.kategorien) return true
        if (r.anweisungen != newAnweisungen) return true
        
        // Zutaten prüfen (Inhaltlich)
        if (existing.ingredients.size != newIngredients.size) return true
        
        val existingSorted = existing.ingredients.sortedBy { it.name }
        val newSorted = newIngredients.sortedBy { it.name }
        
        for (i in existingSorted.indices) {
            val eIng = existingSorted[i]
            val nIng = newSorted[i]
            if (eIng.name != nIng.name || eIng.menge != nIng.menge || eIng.einheit != nIng.einheit) {
                return true
            }
        }

        return false
    }

    private fun parseInstructions(zubereitung: String?): List<String> {
        if (zubereitung.isNullOrBlank()) return emptyList()
        return zubereitung.split(Regex("(?:\r\n|\r|\n)+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun parseIngredients(zutaten: String?): List<Ingredient> {
        if (zutaten.isNullOrBlank()) return emptyList()
        val lines = zutaten.split(Regex("(?:\r\n|\r|\n)+"))
        return lines.mapNotNull { line ->
            parseIngredientLine(line.trim())
        }
    }

    private fun parseIngredientLine(line: String): Ingredient? {
        if (line.isBlank()) return null
        
        val pattern = Pattern.compile("^([\\d,./]+)?\\s*([a-zA-ZäöüÄÖÜß.]+)?\\s*(.*)$")
        val matcher = pattern.matcher(line)
        
        if (matcher.find()) {
            val mengeStr = matcher.group(1)?.replace(",", ".")
            var einheit = matcher.group(2) ?: ""
            var name = matcher.group(3) ?: ""
            
            var menge = try {
                if (mengeStr == null) 0.0 else parseMenge(mengeStr)
            } catch (_: Exception) {
                0.0
            }

            // Umwandlung von Litern in Milliliter (z.B. 1/8 l -> 125 ml)
            if (menge > 0 && (einheit.equals("l", ignoreCase = true) || einheit.equals("liter", ignoreCase = true))) {
                if (menge < 1.0) {
                    menge *= 1000
                    einheit = "ml"
                }
            }

            if (mengeStr != null && name.isBlank()) {
                // Falls der Name leer ist, wurde die Zutat vermutlich als "Einheit" erkannt (z.B. "1 Ei")
                name = einheit
                einheit = ""
            } else if (mengeStr == null) {
                // Keine Zahl am Anfang -> Ganze Zeile ist wahrscheinlich der Name
                name = (einheit + " " + name).trim()
                einheit = ""
            }
            
            return Ingredient(
                rezeptId = 0,
                name = name,
                menge = menge,
                einheit = einheit
            )
        }
        return null
    }

    private fun parseMenge(mengeStr: String): Double {
        return if (mengeStr.contains("/")) {
            val parts = mengeStr.split("/")
            if (parts.size == 2) {
                parts[0].toDouble() / parts[1].toDouble()
            } else 1.0
        } else {
            mengeStr.toDouble()
        }
    }

    private fun formatBildPfad(pfad: String?): String? {
        if (pfad.isNullOrBlank()) return null
        if (pfad.startsWith("http")) return pfad
        val cleanPfad = pfad.removePrefix("/")
        return if (cleanPfad.startsWith("uploads")) {
            "https://rezepte-ds.schroeer-privat.de/$cleanPfad"
        } else {
            "https://rezepte-ds.schroeer-privat.de/uploads/$cleanPfad"
        }
    }
}
