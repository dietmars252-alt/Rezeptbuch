package de.ds.rezeptbuch.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.ds.rezeptbuch.ui.viewmodel.RecipeViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest

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
    val portionen by viewModel.entryPortionen.collectAsState()
    val ingredients by viewModel.entryIngredients.collectAsState()
    val instructions by viewModel.entryInstructions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val arbeitszeit by viewModel.entryArbeitszeit.collectAsState()
    val kochzeit by viewModel.entryKochzeit.collectAsState()
    val notizen by viewModel.entryNotizen.collectAsState()
    val quelle by viewModel.entryQuelle.collectAsState()
    val bildPfad by viewModel.entryBildPfad.collectAsState()
    val kalorien by viewModel.entryKalorien.collectAsState()
    val fett by viewModel.entryFett.collectAsState()
    val eiweis by viewModel.entryEiweis.collectAsState()
    val kohlenhydrate by viewModel.entryKohlenhydrate.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            // Wenn der Nutzer ein Bild ausgewählt hat, speichern wir den Pfad als String
            if (uri != null) {
                viewModel.updateEntryBildPfad(uri.toString())
            }
        }
    )

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
            // NEU: Bild-Auswahlbereich ganz oben im Formular
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = MaterialTheme.shapes.large,
                    onClick = {
                        // Öffnet die Android-Galerie und filtert rein auf Bilder
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Nutzt unsere bestehende RecipeImage Komponente zur Vorschau
                        RecipeImage(
                            bildPfad = bildPfad,
                            contentDescription = "Ausgewähltes Rezeptbild",
                            modifier = Modifier.fillMaxSize()
                        )

                        // Ein kleines dezentes Overlay-Banner, damit man sieht, dass man klicken kann
                        Surface(
                            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                        ) {
                            Text(
                                text = if (bildPfad.isNullOrBlank()) "Bild hinzufügen" else "Bild ändern",
                                color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


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
                val entryKategorien by viewModel.entryKategorien.collectAsState()

                // State für den "Kategorie hinzufügen"-Dialog
                var showAddCategoryDialog by remember { mutableStateOf(false) }
                var newCategoryName by remember { mutableStateOf("") }

                // Der Dialog zum Erstellen einer neuen Kategorie
                if (showAddCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showAddCategoryDialog = false
                            newCategoryName = ""
                        },
                        title = { Text("Neue Kategorie hinzufügen") },
                        text = {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("Name der Kategorie") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        viewModel.addNewCategory(newCategoryName)
                                        showAddCategoryDialog = false
                                        newCategoryName = ""
                                    }
                                }
                            ) {
                                Text("Hinzufügen")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showAddCategoryDialog = false
                                    newCategoryName = ""
                                }
                            ) {
                                Text("Abbrechen")
                            }
                        }
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Kategorien auswählen",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // FlowRow bricht automatisch in die nächste Zeile um, wenn der Platz nicht reicht
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = entryKategorien.contains(category.name)

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleEntryKategorie(category.name) },
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

                        // 2. NEU: Der "+ Hinzufügen" Button als spezieller InputChip am Ende
                        InputChip(
                            selected = false,
                            onClick = { showAddCategoryDialog = true },
                            label = { Text("Neu...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Kategorie hinzufügen",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.4f
                                ),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Arbeitszeit Feld
                    OutlinedTextField(
                        value = arbeitszeit,
                        onValueChange = viewModel::updateEntryArbeitszeit,
                        label = { Text("Arbeitszeit (Min.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    // Kochzeit / Backzeit Feld
                    OutlinedTextField(
                        value = kochzeit,
                        onValueChange = viewModel::updateEntryKochzeit,
                        label = { Text("Koch-/Backzeit (Min.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                val bewertung by viewModel.entryBewertung.collectAsState()

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)) {
                    Text(
                        text = "Bewertung",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            val isStarred = i <= bewertung
                            IconButton(
                                onClick = { viewModel.updateEntryBewertung(if (bewertung == i) 0 else i) }
                            ) {
                                Icon(
                                    imageVector = if (isStarred) Icons.Rounded.Star else Icons.TwoTone.Star,
                                    contentDescription = "$i Sterne",
                                    tint = if (isStarred) androidx.compose.ui.graphics.Color(
                                        0xFFFFB300
                                    ) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (bewertung == 0) "Keine Bewertung" else "$bewertung / 5 Sternen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                                onValueChange = {
                                    viewModel.updateEntryIngredient(
                                        index,
                                        ingredient.copy(menge = it)
                                    )
                                },
                                label = { Text("Menge") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = ingredient.einheit,
                                onValueChange = {
                                    viewModel.updateEntryIngredient(
                                        index,
                                        ingredient.copy(einheit = it)
                                    )
                                },
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
                            onValueChange = {
                                viewModel.updateEntryIngredient(
                                    index,
                                    ingredient.copy(name = it)
                                )
                            },
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Zusatzinformationen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Quelle Feld
                OutlinedTextField(
                    value = quelle,
                    onValueChange = viewModel::updateEntryQuelle,
                    label = { Text("Quelle (Website, Kochbuch...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Nährwerte pro Portion",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = kalorien,
                        onValueChange = viewModel::updateEntryKalorien,
                        label = { Text("Kalorien") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fett,
                        onValueChange = viewModel::updateEntryFett,
                        label = { Text("Fett (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = eiweis,
                        onValueChange = viewModel::updateEntryEiweis,
                        label = { Text("Eiweiß (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = kohlenhydrate,
                        onValueChange = viewModel::updateEntryKohlenhydrate,
                        label = { Text("KH (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notizen Feld (Mehrzeilig für Tipps und Tricks)
                OutlinedTextField(
                    value = notizen,
                    onValueChange = viewModel::updateEntryNotizen,
                    label = { Text("Notizen & Tipps") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
