package de.ds.rezeptbuch.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.ds.rezeptbuch.data.model.Category
import de.ds.rezeptbuch.data.model.RecipeWithIngredients

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    recipes: List<RecipeWithIngredients>,
    categories: List<Category>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onCategoriesClear: () -> Unit,
    showOnlyTopRated: Boolean,
    onToggleShowOnlyTopRated: () -> Unit,
    onRecipeClick: (RecipeWithIngredients) -> Unit,
    onAddRecipeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isGridLayout: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Mein Rezeptbuch (${recipes.size})") },
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Einstellungen")
                    }
                }
            )

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal
                        )
                    )
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Rezepte suchen...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Clear,
                                            contentDescription = "Suche löschen"
                                        )
                                    }
                                }
                                IconButton(onClick = onToggleShowOnlyTopRated) {
                                    Icon(
                                        imageVector = if (showOnlyTopRated) Icons.Rounded.Star else Icons.TwoTone.Star,
                                        contentDescription = "Nur Top-Rezepte",
                                        tint = if (showOnlyTopRated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )

                    CategoryFilters(
                        categories = categories,
                        selectedCategories = selectedCategories,
                        onCategoryToggle = onCategoryToggle,
                        onCategoriesClear = onCategoriesClear
                    )
                }
            }

            val safePadding = WindowInsets.safeDrawing.asPaddingValues()
            val layoutDirection = LocalLayoutDirection.current

            if (isGridLayout) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = safePadding.calculateStartPadding(layoutDirection) + 16.dp,
                        end = safePadding.calculateEndPadding(layoutDirection) + 16.dp,
                        top = 16.dp,
                        bottom = safePadding.calculateBottomPadding() + 80.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recipes) { recipeWithIngredients ->
                        RecipeGridItem(
                            recipeWithIngredients = recipeWithIngredients,
                            onClick = { onRecipeClick(recipeWithIngredients) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = safePadding.calculateStartPadding(layoutDirection),
                        end = safePadding.calculateEndPadding(layoutDirection),
                        bottom = safePadding.calculateBottomPadding() + 80.dp
                    )
                ) {
                    items(recipes) { recipeWithIngredients ->
                        RecipeItem(
                            recipeWithIngredients = recipeWithIngredients,
                            onClick = { onRecipeClick(recipeWithIngredients) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddRecipeClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout)),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Rezept hinzufügen")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilters(
    categories: List<Category>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onCategoriesClear: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedCategories.isNotEmpty()) {
            item {
                InputChip(
                    selected = false,
                    onClick = onCategoriesClear,
                    label = { Text("Filter löschen") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Alle Filter löschen",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
        }

        items(categories) { category ->
            val isSelected = selectedCategories.contains(category.name)
            FilterChip(
                selected = isSelected,
                onClick = { onCategoryToggle(category.name) },
                label = { Text(category.name) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Ausgewählt",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun RecipeGridItem(
    recipeWithIngredients: RecipeWithIngredients,
    onClick: () -> Unit
) {
    val recipe = recipeWithIngredients.recipe
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                RecipeImage(
                    bildPfad = recipe.bildpfad,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = recipe.titel,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun RecipeItem(
    recipeWithIngredients: RecipeWithIngredients,
    onClick: () -> Unit
) {
    val recipe = recipeWithIngredients.recipe
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Abstand zwischen Bild und Text
        ) {
            // NEU: Kleines quadratisches Vorschaubild auf der linken Seite
            androidx.compose.material3.Card(
                modifier = Modifier.size(56.dp), // Angenehme Thumbnail-Größe
                shape = MaterialTheme.shapes.medium
            ) {
                RecipeImage(
                    bildPfad = recipe.bildpfad,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Der Text-Block wandert nach rechts daneben
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.titel,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
