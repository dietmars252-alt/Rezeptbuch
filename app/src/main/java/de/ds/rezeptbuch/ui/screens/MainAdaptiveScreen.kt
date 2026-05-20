package de.ds.rezeptbuch.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.ds.rezeptbuch.ui.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainAdaptiveScreen(
    viewModel: RecipeViewModel,
    modifier: Modifier = Modifier
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>()
    val scope = rememberCoroutineScope()
    
    var showEntryScreen by remember { mutableStateOf(false) }

    val recipes by viewModel.filteredRecipes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()

    BackHandler(enabled = showEntryScreen || navigator.canNavigateBack()) {
        if (showEntryScreen) {
            showEntryScreen = false
        } else {
            scope.launch {
                navigator.navigateBack()
            }
        }
    }

    if (showEntryScreen) {
        RecipeEntryScreen(
            viewModel = viewModel,
            onBackClick = { showEntryScreen = false },
            onSaveSuccess = { showEntryScreen = false }
        )
    } else {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecipeListScreen(
                        recipes = recipes,
                        categories = categories,
                        searchQuery = searchQuery,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        selectedCategories = selectedCategories,
                        onCategoryToggle = viewModel::toggleCategory,
                        onCategoriesClear = viewModel::clearCategories,
                        showOnlyFavorites = showOnlyFavorites,
                        onToggleShowOnlyFavorites = viewModel::toggleShowOnlyFavorites,
                        onRecipeClick = { recipeWithIngredients ->
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, recipeWithIngredients.recipe.id)
                            }
                        },
                        onToggleFavorite = viewModel::toggleFavorite,
                        onAddRecipeClick = { 
                            viewModel.startNewRecipe()
                            showEntryScreen = true 
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            detailPane = {
            Surface(modifier = Modifier.fillMaxSize()) {
                val selectedRecipeId = navigator.currentDestination?.contentKey
                val selectedRecipe = recipes.find { it.recipe.id == selectedRecipeId }
                
                if (selectedRecipe != null) {
                    val currentPortions by viewModel.getPortionsForRecipe(
                        selectedRecipe.recipe.id, 
                        selectedRecipe.recipe.portionen
                    ).collectAsState()

                    RecipeDetailScreen(
                        recipeWithIngredients = selectedRecipe,
                        currentPortions = currentPortions,
                        onUpdatePortions = { viewModel.updatePortions(selectedRecipe.recipe.id, it) },
                        onToggleFavorite = viewModel::toggleFavorite,
                        onBackClick = {
                            scope.launch {
                                navigator.navigateBack()
                            }
                        },
                        onEditClick = {
                            viewModel.startEditing(it)
                            showEntryScreen = true
                        },
                        onDeleteClick = { id ->
                            scope.launch {
                                viewModel.deleteRecipe(id)
                                navigator.navigateBack()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    RecipeDetailScreen(
                        recipeWithIngredients = null,
                        currentPortions = 0,
                        onUpdatePortions = {},
                        onToggleFavorite = { _, _ -> },
                        onBackClick = {
                            scope.launch {
                                navigator.navigateBack()
                            }
                        },
                        onEditClick = {},
                        onDeleteClick = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
}
