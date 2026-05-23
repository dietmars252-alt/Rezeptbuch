package de.ds.rezeptbuch.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.ds.rezeptbuch.data.model.RecipeWithIngredients
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    recipeWithIngredients: RecipeWithIngredients?,
    currentPortions: Int,
    onUpdatePortions: (Int) -> Unit,
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
    val scope = rememberCoroutineScope()

    // Durch das übergeordnete key() startet dieser PagerState garantiert bei 0
    val pagerState = rememberPagerState(
        initialPage = 0
    ) { 3 }

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
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { onEditClick(recipeWithIngredients) }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Bearbeiten")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Löschen")
                }
            }
        )

        // Die Tab-Leiste zur Navigation
        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            val tabs = listOf("Info", "Zutaten", "Schritte")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(title) }
                )
            }
        }

        // Der HorizontalPager für das Swipen
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) { pageIndex ->
            val safeBottomPadding =
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp

            when (pageIndex) {
                0 -> { // SEITE 1: ALLGEMEINE INFOS
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = safeBottomPadding),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                RecipeImage(
                                    bildPfad = recipe.bildpfad,
                                    contentDescription = recipe.titel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FlowRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    recipe.kategorien.forEach { kat ->
                                        AssistChip(onClick = {}, label = { Text(kat) })
                                    }
                                }
                                Row {
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = if (i <= recipe.bewertung) Icons.Rounded.Star else Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = if (i <= recipe.bewertung) androidx.compose.ui.graphics.Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (recipe.arbeitszeit != null) Text("Arbeitszeit: ${recipe.arbeitszeit} Min.", style = MaterialTheme.typography.bodyLarge)
                                    if (recipe.kochzeit != null) Text("Koch-/Backzeit: ${recipe.kochzeit} Min.", style = MaterialTheme.typography.bodyLarge)
                                    if (!recipe.quelle.isNullOrBlank()) Text("Quelle: ${recipe.quelle}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        if (!recipe.notizen.isNullOrBlank()) {
                            item {
                                Text("Notizen", style = MaterialTheme.typography.titleLarge)
                                Text(recipe.notizen, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                1 -> { // SEITE 2: ZUTATEN
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = safeBottomPadding)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Portionen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { onUpdatePortions(currentPortions - 1) }) { Icon(Icons.Rounded.Remove, "Weniger") }
                                        Text(currentPortions.toString(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 8.dp))
                                        IconButton(onClick = { onUpdatePortions(currentPortions + 1) }) { Icon(Icons.Rounded.Add, "Mehr") }
                                    }
                                }
                            }
                        }

                        items(ingredients) { ingredient ->
                            val scaledAmount = (ingredient.menge / recipe.portionen) * currentPortions
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("•", fontWeight = FontWeight.Bold)
                                Text("${decimalFormat.format(scaledAmount)} ${ingredient.einheit} ${ingredient.name}")
                            }
                        }
                    }
                }

                2 -> { // SEITE 3: ZUBEREITUNG
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = safeBottomPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recipe.anweisungen.withIndex().toList()) { (index, step) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "${index + 1}.",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                    Text(step, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}