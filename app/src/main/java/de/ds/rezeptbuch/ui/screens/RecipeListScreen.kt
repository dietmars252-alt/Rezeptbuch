package de.ds.rezeptbuch.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    showOnlyFavorites: Boolean,
    onToggleShowOnlyFavorites: () -> Unit,
    onRecipeClick: (RecipeWithIngredients) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onAddRecipeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
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
                                IconButton(onClick = onToggleShowOnlyFavorites) {
                                    Icon(
                                        imageVector = if (showOnlyFavorites) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Nur Favoriten",
                                        tint = if (showOnlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(recipes) { recipeWithIngredients ->
                    RecipeItem(
                        recipeWithIngredients = recipeWithIngredients,
                        onClick = { onRecipeClick(recipeWithIngredients) },
                        onToggleFavorite = { isFav -> 
                            onToggleFavorite(recipeWithIngredients.recipe.id, isFav) 
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        LargeFloatingActionButton(
            onClick = onAddRecipeClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Rezept hinzufügen")
        }
    }
}

@Composable
fun CategoryFilters(
    categories: List<Category>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onCategoriesClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedCard(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedCategories.isEmpty()) {
                        "Alle Kategorien"
                    } else {
                        "Kategorien (${selectedCategories.size})"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            DropdownMenuItem(
                text = { Text("Alle auswählen / zurücksetzen", fontWeight = FontWeight.Bold) },
                onClick = {
                    onCategoriesClear()
                    expanded = false
                }
            )
            HorizontalDivider()
            categories.forEach { category ->
                val isSelected = selectedCategories.contains(category.name)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null // Handled by MenuItem click
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.name)
                        }
                    },
                    onClick = { onCategoryToggle(category.name) }
                )
            }
        }
    }
}

@Composable
fun RecipeItem(
    recipeWithIngredients: RecipeWithIngredients,
    onClick: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit
) {
    val recipe = recipeWithIngredients.recipe
    // Radical simplification: Replaced ListItem with a simple Row and Column
    // to avoid persistent measurement crashes in ListDetailPaneScaffold.
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.titel,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${recipe.kategorien} • ${recipe.portionen} Portionen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onToggleFavorite(!recipe.istFavorit) }) {
                Icon(
                    imageVector = if (recipe.istFavorit) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorit",
                    tint = if (recipe.istFavorit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
