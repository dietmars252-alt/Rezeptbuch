package de.ds.rezeptbuch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.ds.rezeptbuch.data.model.RecipeWithIngredients
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    recipeWithIngredients: RecipeWithIngredients?,
    currentPortions: Int,
    onUpdatePortions: (Int) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onBackClick: () -> Unit,
    onEditClick: (RecipeWithIngredients) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (recipeWithIngredients == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Wähle ein Rezept aus", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val recipe = recipeWithIngredients.recipe
    val ingredients = recipeWithIngredients.ingredients
    val decimalFormat = DecimalFormat("#.##")

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Rezept löschen?") },
            text = { Text("Möchtest du das Rezept '${recipe.titel}' wirklich dauerhaft löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(recipe.id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(recipe.titel) },
            windowInsets = WindowInsets.statusBars,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Zurück"
                    )
                }
            },
            actions = {
                IconButton(onClick = { onEditClick(recipeWithIngredients) }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Bearbeiten")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Löschen")
                }
                IconButton(onClick = { onToggleFavorite(recipe.id, !recipe.istFavorit) }) {
                    Icon(
                        imageVector = if (recipe.istFavorit) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorit",
                        tint = if (recipe.istFavorit) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Hier werden die mehreren Kategorien als Chips dargestellt
                // Context-Check: Falls dein Datenmodell noch eine kommagetrennte Liste ist,
                // kannst du es hier mit .split(",") trennen (siehe Hinweis unten).
                val kategorien = remember(recipe.kategorien) {
                    // Beispiel-Logik: Falls du dein Datenmodell schon auf List<String> umgestellt hast
                    recipe.kategorien
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    kategorien.forEach { kategorie ->
                        AssistChip(
                            onClick = { /* Optional: Nach dieser Kategorie filtern */ },
                            label = { Text(kategorie) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Portion Calculator
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Portionen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onUpdatePortions(currentPortions - 1) }) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Verringern")
                            }
                            Text(
                                text = currentPortions.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { onUpdatePortions(currentPortions + 1) }) {
                                Icon(Icons.Rounded.Add, contentDescription = "Erhöhen")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Zutaten", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(ingredients) { ingredient ->
                val scaledAmount = (ingredient.menge / recipe.portionen) * currentPortions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${decimalFormat.format(scaledAmount)} ${ingredient.einheit} ${ingredient.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Zubereitung", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recipe.anweisungen.withIndex().toList()) { (index, step) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "${index + 1}.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            step,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}