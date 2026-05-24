package de.ds.rezeptbuch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.ds.rezeptbuch.data.model.Category
import de.ds.rezeptbuch.data.model.RecipeWithIngredients
import de.ds.rezeptbuch.data.remote.JsonImportService
import de.ds.rezeptbuch.data.remote.McbImportService
import de.ds.rezeptbuch.data.repository.RecipeRepository
import de.ds.rezeptbuch.ui.theme.AppColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

class RecipeViewModel(
    application: Application,
    private val repository: RecipeRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _portionMap = MutableStateFlow<Map<Long, Int>>(emptyMap())

    fun getPortionsForRecipe(recipeId: Long, defaultPortions: Int): StateFlow<Int> {
        return _portionMap.map { it[recipeId] ?: defaultPortions }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultPortions)
    }

    fun updatePortions(recipeId: Long, newPortions: Int) {
        if (newPortions < 1) return
        val currentMap = _portionMap.value.toMutableMap()
        currentMap[recipeId] = newPortions
        _portionMap.value = currentMap
    }

    private val _showOnlyTopRated = MutableStateFlow(false)
    val showOnlyTopRated: StateFlow<Boolean> = _showOnlyTopRated

    val filteredRecipes: StateFlow<List<RecipeWithIngredients>> = combine(
        repository.allRecipes,
        _searchQuery,
        _selectedCategories,
        _showOnlyTopRated
    ) { recipes, query, categories, onlyTopRated ->
        recipes.filter { recipeWithIngredients ->
            val recipe = recipeWithIngredients.recipe
            val matchesQuery = recipe.titel.contains(query, ignoreCase = true)
            val matchesCategory =
                categories.isEmpty() || recipe.kategorien.any { categories.contains(it) }

            val matchesTopRated = !onlyTopRated || recipe.bewertung >= 4

            matchesQuery && matchesCategory && matchesTopRated
        }.sortedBy { it.recipe.titel.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleShowOnlyTopRated() {
        _showOnlyTopRated.value = !_showOnlyTopRated.value
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleCategory(category: String) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _selectedCategories.value = current
    }

    fun clearCategories() {
        _selectedCategories.value = emptySet()
    }

    fun deleteRecipe(recipeId: Long) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
        }
    }

    // Recipe Entry State
    private val _editingRecipeId = MutableStateFlow<Long?>(null)
    val editingRecipeId: StateFlow<Long?> = _editingRecipeId

    private val _entryTitel = MutableStateFlow("")
    val entryTitel: StateFlow<String> = _entryTitel

    private val _entryTitelError = MutableStateFlow(false)
    val entryTitelError: StateFlow<Boolean> = _entryTitelError

    private val _entryKategorien = MutableStateFlow<Set<String>>(emptySet())
    val entryKategorien: StateFlow<Set<String>> = _entryKategorien

    private val _entryPortionen = MutableStateFlow(4)
    val entryPortionen: StateFlow<Int> = _entryPortionen

    data class IngredientEntry(val name: String, val menge: String, val einheit: String)

    private val _entryIngredients = MutableStateFlow(listOf(IngredientEntry("", "", "")))
    val entryIngredients: StateFlow<List<IngredientEntry>> = _entryIngredients

    private val _entryInstructions = MutableStateFlow(listOf(""))
    val entryInstructions: StateFlow<List<String>> = _entryInstructions

    private val _entryArbeitszeit = MutableStateFlow("")
    val entryArbeitszeit: StateFlow<String> = _entryArbeitszeit

    private val _entryKochzeit = MutableStateFlow("")
    val entryKochzeit: StateFlow<String> = _entryKochzeit

    private val _entryNotizen = MutableStateFlow("")
    val entryNotizen: StateFlow<String> = _entryNotizen

    private val _entryQuelle = MutableStateFlow("")
    val entryQuelle: StateFlow<String> = _entryQuelle

    private val _entryBewertung = MutableStateFlow(0)
    val entryBewertung: StateFlow<Int> = _entryBewertung

    private val _entryBildPfad = MutableStateFlow<String?>(null)
    val entryBildPfad: StateFlow<String?> = _entryBildPfad

    private val _isDarkTheme = MutableStateFlow<Boolean?>(null)
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme

    private val _appColorScheme = MutableStateFlow(AppColorScheme.GREEN)
    val appColorScheme: StateFlow<AppColorScheme> = _appColorScheme

    fun toggleTheme(isDark: Boolean?) {
        _isDarkTheme.value = isDark
    }

    fun updateColorScheme(scheme: AppColorScheme) {
        _appColorScheme.value = scheme
    }

    private val _entryKalorien = MutableStateFlow("")
    val entryKalorien: StateFlow<String> = _entryKalorien

    private val _entryFett = MutableStateFlow("")
    val entryFett: StateFlow<String> = _entryFett

    private val _entryEiweis = MutableStateFlow("")
    val entryEiweis: StateFlow<String> = _entryEiweis

    private val _entryKohlenhydrate = MutableStateFlow("")
    val entryKohlenhydrate: StateFlow<String> = _entryKohlenhydrate

    private val importService = JsonImportService(repository)
    private val mcbImportService = McbImportService(application, repository)

    fun importRecipesFromJson(jsonString: String) {
        viewModelScope.launch {
            importService.importJson(jsonString)
        }
    }

    fun importRecipesFromMcb(inputStream: InputStream) {
        viewModelScope.launch {
            mcbImportService.importMcb(inputStream)
        }
    }

    suspend fun exportRecipesToJson(): String {
        return importService.exportJson()
    }

    fun updateEntryBewertung(value: Int) {
        _entryBewertung.value = value.coerceIn(0, 5)
    }

    fun updateEntryArbeitszeit(value: String) { _entryArbeitszeit.value = value }
    fun updateEntryKochzeit(value: String) { _entryKochzeit.value = value }
    fun updateEntryNotizen(value: String) { _entryNotizen.value = value }
    fun updateEntryQuelle(value: String) { _entryQuelle.value = value }
    fun updateEntryKalorien(value: String) { _entryKalorien.value = value }
    fun updateEntryFett(value: String) { _entryFett.value = value }
    fun updateEntryEiweis(value: String) { _entryEiweis.value = value }
    fun updateEntryKohlenhydrate(value: String) { _entryKohlenhydrate.value = value }

    fun updateEntryTitel(value: String) {
        _entryTitel.value = value
        if (value.isNotBlank()) {
            _entryTitelError.value = false
        }
    }

    fun toggleEntryKategorie(category: String) {
        val current = _entryKategorien.value.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _entryKategorien.value = current
    }

    fun addNewCategory(categoryName: String) {
        if (categoryName.isBlank()) return
        viewModelScope.launch {
            val newCategory = Category(name = categoryName.trim())
            repository.insertCategories(listOf(newCategory))
        }
    }

    fun updateEntryBildPfad(value: String?) { _entryBildPfad.value = value }

    fun updateEntryPortionen(value: Int) {
        if (value > 0) _entryPortionen.value = value
    }

    fun addEntryIngredient() {
        _entryIngredients.value = _entryIngredients.value + IngredientEntry("", "", "")
    }

    fun updateEntryIngredient(index: Int, ingredient: IngredientEntry) {
        val list = _entryIngredients.value.toMutableList()
        if (index in list.indices) {
            list[index] = ingredient
            _entryIngredients.value = list
        }
    }

    fun removeEntryIngredient(index: Int) {
        val list = _entryIngredients.value.toMutableList()
        if (list.size > 1 && index in list.indices) {
            list.removeAt(index)
            _entryIngredients.value = list
        }
    }

    fun addEntryInstruction() {
        _entryInstructions.value = _entryInstructions.value + ""
    }

    fun updateEntryInstruction(index: Int, value: String) {
        val list = _entryInstructions.value.toMutableList()
        if (index in list.indices) {
            list[index] = value
            _entryInstructions.value = list
        }
    }

    fun removeEntryInstruction(index: Int) {
        val list = _entryInstructions.value.toMutableList()
        if (list.size > 1 && index in list.indices) {
            list.removeAt(index)
            _entryInstructions.value = list
        }
    }

    fun saveNewRecipe(onSuccess: () -> Unit) {
        if (_entryTitel.value.isBlank()) {
            _entryTitelError.value = true
            return
        }

        viewModelScope.launch {
            val recipe = de.ds.rezeptbuch.data.model.Recipe(
                id = _editingRecipeId.value ?: 0,
                titel = _entryTitel.value,
                kategorien = _entryKategorien.value.toList(),
                portionen = _entryPortionen.value,
                anweisungen = _entryInstructions.value.filter { it.isNotBlank() },
                bewertung = _entryBewertung.value,
                arbeitszeit = _entryArbeitszeit.value.toIntOrNull(),
                kochzeit = _entryKochzeit.value.toIntOrNull(),
                notizen = _entryNotizen.value,
                quelle = _entryQuelle.value,
                bildpfad = _entryBildPfad.value,
                kalorien = _entryKalorien.value.toIntOrNull(),
                kohlenhydrate = _entryKohlenhydrate.value.toFloatOrNull(),
                eiweis = _entryEiweis.value.toFloatOrNull(),
                fett = _entryFett.value.toFloatOrNull()
            )
            val ingredients = _entryIngredients.value.filter { it.name.isNotBlank() }.map {
                de.ds.rezeptbuch.data.model.Ingredient(
                    rezeptId = recipe.id,
                    name = it.name,
                    menge = it.menge.toDoubleOrNull() ?: 0.0,
                    einheit = it.einheit
                )
            }

            if (_editingRecipeId.value == null) {
                repository.insertRecipeWithIngredients(recipe, ingredients)
            } else {
                repository.updateRecipeWithIngredients(recipe, ingredients)
            }

            resetEntryForm()
            onSuccess()
        }
    }

    fun startEditing(recipeWithIngredients: RecipeWithIngredients) {
        _editingRecipeId.value = recipeWithIngredients.recipe.id
        _entryTitel.value = recipeWithIngredients.recipe.titel
        _entryKategorien.value = recipeWithIngredients.recipe.kategorien.toSet()
        _entryPortionen.value = recipeWithIngredients.recipe.portionen
        _entryIngredients.value = recipeWithIngredients.ingredients.map {
            IngredientEntry(it.name, it.menge.toString(), it.einheit)
        }
        _entryInstructions.value = recipeWithIngredients.recipe.anweisungen
        _entryBewertung.value = recipeWithIngredients.recipe.bewertung
        _entryArbeitszeit.value = recipeWithIngredients.recipe.arbeitszeit?.toString() ?: ""
        _entryKochzeit.value = recipeWithIngredients.recipe.kochzeit?.toString() ?: ""
        _entryNotizen.value = recipeWithIngredients.recipe.notizen ?: ""
        _entryQuelle.value = recipeWithIngredients.recipe.quelle ?: ""
        _entryBildPfad.value = recipeWithIngredients.recipe.bildpfad
        _entryKalorien.value = recipeWithIngredients.recipe.kalorien?.toString() ?: ""
        _entryFett.value = recipeWithIngredients.recipe.fett?.toString() ?: ""
        _entryEiweis.value = recipeWithIngredients.recipe.eiweis?.toString() ?: ""
        _entryKohlenhydrate.value = recipeWithIngredients.recipe.kohlenhydrate?.toString() ?: ""
    }

    fun startNewRecipe() {
        resetEntryForm()
    }

    private fun resetEntryForm() {
        _editingRecipeId.value = null
        _entryTitel.value = ""
        _entryTitelError.value = false
        _entryKategorien.value = emptySet()
        _entryPortionen.value = 4
        _entryIngredients.value = listOf(IngredientEntry("", "", ""))
        _entryInstructions.value = listOf("")
        _entryBewertung.value = 0
        _entryArbeitszeit.value = ""
        _entryKochzeit.value = ""
        _entryNotizen.value = ""
        _entryQuelle.value = ""
        _entryBildPfad.value = null
        _entryKalorien.value = ""
        _entryFett.value = ""
        _entryEiweis.value = ""
        _entryKohlenhydrate.value = ""
    }
}

class RecipeViewModelFactory(
    private val application: Application,
    private val repository: RecipeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
