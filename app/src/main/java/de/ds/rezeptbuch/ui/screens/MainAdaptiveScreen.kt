package de.ds.rezeptbuch.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.ds.rezeptbuch.ui.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainAdaptiveScreen(
    viewModel: RecipeViewModel,
    modifier: Modifier = Modifier
) {
    // 1. Wir berechnen die Adaptive Info (V2 für L & XL Support)
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    
    // 2. Wir erzwingen IMMER den Ein-Spalten-Modus, egal wie groß das Display ist.
    val customDirective = calculatePaneScaffoldDirective(adaptiveInfo).copy(
        maxHorizontalPartitions = 1
    )

    // 3. Navigator mit der erzwingenden Directive initialisieren
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>(
        scaffoldDirective = customDirective
    )
    
    val scope = rememberCoroutineScope()

    var showEntryScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }

    val recipes by viewModel.filteredRecipes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyTopRated.collectAsState()
    val keepScreenOnGlobal by viewModel.keepScreenOn.collectAsState()
    val isGridLayout by viewModel.isGridLayout.collectAsState()

    BackHandler(enabled = showEntryScreen || showSettingsScreen || navigator.canNavigateBack()) {
        if (showEntryScreen) {
            showEntryScreen = false
        } else if (showSettingsScreen) {
            showSettingsScreen = false
        } else {
            scope.launch {
                navigator.navigateBack()
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (showEntryScreen) {
            RecipeEntryScreen(
                viewModel = viewModel,
                onBackClick = { showEntryScreen = false },
                onSaveSuccess = { showEntryScreen = false }
            )
        } else if (showSettingsScreen) {
            SettingsScreen(
                viewModel = viewModel,
                onImportJsonClick = viewModel::importRecipesFromJson,
                onImportMcbClick = viewModel::importRecipesFromMcb,
                onExportJsonClick = viewModel::exportRecipesToJson,
                onBackClick = { showSettingsScreen = false }
            )
        } else {
            ListDetailPaneScaffold(
                directive = customDirective,
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
                            showOnlyTopRated = showOnlyFavorites,
                            onToggleShowOnlyTopRated = viewModel::toggleShowOnlyTopRated,
                            onRecipeClick = { recipeWithIngredients ->
                                scope.launch {
                                    navigator.navigateTo(
                                        ListDetailPaneScaffoldRole.Detail,
                                        recipeWithIngredients.recipe.id
                                    )
                                }
                            },
                            onAddRecipeClick = {
                                viewModel.startNewRecipe()
                                showEntryScreen = true
                            },
                            onSettingsClick = { showSettingsScreen = true },
                            isGridLayout = isGridLayout,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                detailPane = {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val selectedRecipeId = navigator.currentDestination?.contentKey
                        val selectedRecipe = recipes.find { it.recipe.id == selectedRecipeId }

                        key(selectedRecipeId) {
                            if (selectedRecipe != null) {
                                val currentPortions by viewModel.getPortionsForRecipe(
                                    selectedRecipe.recipe.id,
                                    selectedRecipe.recipe.portionen
                                ).collectAsState()

                                RecipeDetailScreen(
                                    recipeWithIngredients = selectedRecipe,
                                    currentPortions = currentPortions,
                                    keepScreenOnGlobal = keepScreenOnGlobal,
                                    onUpdatePortions = {
                                        viewModel.updatePortions(
                                            selectedRecipe.recipe.id,
                                            it
                                        )
                                    },
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
                                    keepScreenOnGlobal = false,
                                    onUpdatePortions = {},
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
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
