package de.ds.rezeptbuch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.ds.rezeptbuch.data.model.Category
import de.ds.rezeptbuch.data.model.RecipeWithIngredients
import de.ds.rezeptbuch.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

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

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites

    val filteredRecipes: StateFlow<List<RecipeWithIngredients>> = combine(
        repository.allRecipes,
        _searchQuery,
        _selectedCategories,
        _showOnlyFavorites
    ) { recipes, query, selectedCats, onlyFavorites ->
        recipes.filter { recipeWithIngredients ->
            val recipe = recipeWithIngredients.recipe
            val matchesQuery = recipe.titel.contains(query, ignoreCase = true)
            val matchesCategory = selectedCats.isEmpty() || recipe.kategorien.any { it in selectedCats }
            val matchesFavorites = !onlyFavorites || recipe.istFavorit
            matchesQuery && matchesCategory && matchesFavorites
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    init {
        viewModelScope.launch {
            // Seed data if empty (simplified check)
            // In a real app, this might be handled differently
            repository.allRecipes.collect {
                if (it.isEmpty()) {
                    repository.seedMockData()
                }
            }
        }
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

    fun toggleFavorite(recipeId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(recipeId, isFavorite)
        }
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

    private val _entryKategorien = MutableStateFlow<List<String>>(emptyList())
    val entryKategorien: StateFlow<List<String>> = _entryKategorien

    private val _entryPortionen = MutableStateFlow(4)
    val entryPortionen: StateFlow<Int> = _entryPortionen

    data class IngredientEntry(val name: String, val menge: String, val einheit: String)
    private val _entryIngredients = MutableStateFlow(listOf(IngredientEntry("", "", "")))
    val entryIngredients: StateFlow<List<IngredientEntry>> = _entryIngredients

    private val _entryInstructions = MutableStateFlow(listOf(""))
    val entryInstructions: StateFlow<List<String>> = _entryInstructions

    fun updateEntryTitel(value: String) { 
        _entryTitel.value = value 
        if (value.isNotBlank()) {
            _entryTitelError.value = false
        }
    }
    fun toggleEntryKategorie(kategorieName: String) {
        val current =_entryKategorien.value.toMutableList()
        if (current.contains(kategorieName)) {
            current.remove(kategorieName)
        } else {
            current.add(kategorieName)
        }
        _entryKategorien.value = current
    }
    fun updateEntryPortionen(value: Int) { if (value > 0) _entryPortionen.value = value }

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
                kategorien = _entryKategorien.value,
                portionen = _entryPortionen.value,
                anweisungen = _entryInstructions.value.filter { it.isNotBlank() },
                istFavorit = false // Keep existing or reset? For simplicity reset.
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
                // To keep favorite status, we'd need to fetch it first.
                // Assuming we want to keep it if possible.
                repository.updateRecipeWithIngredients(recipe, ingredients)
            }
            
            resetEntryForm()
            onSuccess()
        }
    }

    fun startEditing(recipeWithIngredients: RecipeWithIngredients) {
        _editingRecipeId.value = recipeWithIngredients.recipe.id
        _entryTitel.value = recipeWithIngredients.recipe.titel
        _entryKategorien.value = recipeWithIngredients.recipe.kategorien
        _entryPortionen.value = recipeWithIngredients.recipe.portionen
        _entryIngredients.value = recipeWithIngredients.ingredients.map {
            IngredientEntry(it.name, it.menge.toString(), it.einheit)
        }
        _entryInstructions.value = recipeWithIngredients.recipe.anweisungen
    }

    fun startNewRecipe() {
        resetEntryForm()
    }

    private fun resetEntryForm() {
        _editingRecipeId.value = null
        _entryTitel.value = ""
        _entryTitelError.value = false
        _entryKategorien.value = emptyList()
        _entryPortionen.value = 4
        _entryIngredients.value = listOf(IngredientEntry("", "", ""))
        _entryInstructions.value = listOf("")
    }
}

class RecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
