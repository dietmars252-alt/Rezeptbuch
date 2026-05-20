package de.ds.rezeptbuch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.ds.rezeptbuch.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEntryScreen(
    viewModel: RecipeViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editingId by viewModel.editingRecipeId.collectAsState()
    val titel by viewModel.entryTitel.collectAsState()
    val titelError by viewModel.entryTitelError.collectAsState()
    val kategorie by viewModel.entryKategorien.collectAsState()
    val portionen by viewModel.entryPortionen.collectAsState()
    val ingredients by viewModel.entryIngredients.collectAsState()
    val instructions by viewModel.entryInstructions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (editingId == null) "Neues Rezept" else "Rezept bearbeiten") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Abbrechen",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.saveNewRecipe(onSaveSuccess) }) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = "Speichern",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            windowInsets = WindowInsets.statusBars
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = titel,
                    onValueChange = viewModel::updateEntryTitel,
                    label = { Text("Titel") },
                    isError = titelError,
                    supportingText = {
                        if (titelError) {
                            Text("Der Titel darf nicht leer sein")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Kategorien auswählen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Wir beobachten die gewählte Liste aus dem ViewModel
                val ausgewaehlteKategorien by viewModel.entryKategorien.collectAsState()

                // FlowRow bricht automatisch in die nächste Zeile um, wenn der Bildschirm voll ist

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = ausgewaehlteKategorien.contains(category.name)

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleEntryKategorie(category.name) },
                            label = { Text(category.name) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = portionen.toString(),
                    onValueChange = { viewModel.updateEntryPortionen(it.toIntOrNull() ?: 0) },
                    label = { Text("Portionen") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text("Zutaten", style = MaterialTheme.typography.titleMedium)
            }

            itemsIndexed(ingredients) { index, ingredient ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = ingredient.menge,
                                onValueChange = { viewModel.updateEntryIngredient(index, ingredient.copy(menge = it)) },
                                label = { Text("Menge") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = ingredient.einheit,
                                onValueChange = { viewModel.updateEntryIngredient(index, ingredient.copy(einheit = it)) },
                                label = { Text("Einheit") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = { viewModel.removeEntryIngredient(index) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Löschen")
                            }
                        }
                        
                        OutlinedTextField(
                            value = ingredient.name,
                            onValueChange = { viewModel.updateEntryIngredient(index, ingredient.copy(name = it)) },
                            label = { Text("Zutat") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                TextButton(
                    onClick = viewModel::addEntryIngredient,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Zutat hinzufügen")
                }
            }

            item {
                Text("Anleitung", style = MaterialTheme.typography.titleMedium)
            }

            itemsIndexed(instructions) { index, instruction ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = instruction,
                        onValueChange = { viewModel.updateEntryInstruction(index, it) },
                        label = { Text("Schritt ${index + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.removeEntryInstruction(index) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Löschen")
                    }
                }
            }

            item {
                TextButton(
                    onClick = viewModel::addEntryInstruction,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Schritt hinzufügen")
                }
            }
            
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
