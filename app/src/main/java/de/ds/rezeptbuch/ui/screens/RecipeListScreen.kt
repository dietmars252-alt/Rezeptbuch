package de.ds.rezeptbuch.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.ds.rezeptbuch.data.model.Category
import de.ds.rezeptbuch.data.model.RecipeWithIngredients
import de.ds.rezeptbuch.ui.theme.AppColorScheme
import kotlinx.coroutines.launch
import java.io.InputStream

private enum class MenuType {
    MAIN, BACKUP, DESIGN
}

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
    onImportJsonClick: (String) -> Unit,
    onImportMcbClick: (InputStream) -> Unit,
    onExportJsonClick: suspend () -> String,
    isDarkTheme: Boolean?,
    onThemeToggle: (Boolean?) -> Unit,
    colorScheme: AppColorScheme,
    onColorSchemeChange: (AppColorScheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var currentSubMenu by remember { mutableStateOf(MenuType.MAIN) }

    // Launcher zum Erstellen einer Backup-Datei (Export)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val jsonString = onExportJsonClick()
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Launcher zum Auswählen einer Backup-Datei (Wiederherstellen/Import)
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().use { reader -> reader.readText() }
                    onImportJsonClick(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Launcher zum Auswählen einer .mcb Datei (My Cookbook Import)
    val mcbLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.let { inputStream ->
                    onImportMcbClick(inputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                    IconButton(onClick = { 
                        currentSubMenu = MenuType.MAIN
                        showMenu = true 
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü öffnen")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        when (currentSubMenu) {
                            MenuType.MAIN -> {
                                DropdownMenuItem(
                                    text = { Text("Backup & Import") },
                                    onClick = { currentSubMenu = MenuType.BACKUP },
                                    leadingIcon = { Icon(Icons.Rounded.Save, null) },
                                    trailingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, null, modifier = Modifier.size(12.dp)) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Darstellung") },
                                    onClick = { currentSubMenu = MenuType.DESIGN },
                                    leadingIcon = { Icon(Icons.Rounded.Palette, null) },
                                    trailingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, null, modifier = Modifier.size(12.dp)) }
                                )
                            }
                            MenuType.BACKUP -> {
                                DropdownMenuItem(
                                    text = { Text("Zurück") },
                                    onClick = { currentSubMenu = MenuType.MAIN },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Backup erstellen") },
                                    onClick = {
                                        showMenu = false
                                        createDocumentLauncher.launch("rezepte_backup.json")
                                    },
                                    leadingIcon = { Icon(Icons.Rounded.Save, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Backup wiederherstellen") },
                                    onClick = {
                                        showMenu = false
                                        restoreLauncher.launch("application/json")
                                    },
                                    leadingIcon = { Icon(Icons.Rounded.Refresh, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("My Cookbook (.mcb) importieren") },
                                    onClick = {
                                        showMenu = false
                                        mcbLauncher.launch("*/*")
                                    },
                                    leadingIcon = { Icon(Icons.Rounded.FileDownload, null) }
                                )
                            }
                            MenuType.DESIGN -> {
                                DropdownMenuItem(
                                    text = { Text("Zurück") },
                                    onClick = { currentSubMenu = MenuType.MAIN },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) }
                                )
                                HorizontalDivider()
                                // Theme Mode
                                Text("Modus", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                DropdownMenuItem(
                                    text = { Text("System") },
                                    onClick = { onThemeToggle(null); showMenu = false },
                                    trailingIcon = { if (isDarkTheme == null) Icon(Icons.Rounded.Check, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Hell") },
                                    onClick = { onThemeToggle(false); showMenu = false },
                                    trailingIcon = { if (isDarkTheme == false) Icon(Icons.Rounded.Check, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Dunkel") },
                                    onClick = { onThemeToggle(true); showMenu = false },
                                    trailingIcon = { if (isDarkTheme == true) Icon(Icons.Rounded.Check, null) }
                                )
                                HorizontalDivider()
                                // Color Scheme
                                Text("Farbschema", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                AppColorScheme.entries.forEach { scheme ->
                                    DropdownMenuItem(
                                        text = { Text(scheme.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = { onColorSchemeChange(scheme); showMenu = false },
                                        trailingIcon = { if (colorScheme == scheme) Icon(Icons.Rounded.Check, null) }
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${recipe.kategorien.joinToString(", ")} • ${recipe.portionen} Portionen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // NEU: Reine Anzeige-Sterne ohne umschließenden IconButton
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    val isStarred = i <= recipe.bewertung
                    Icon(
                        imageVector = if (isStarred) Icons.Rounded.Star else Icons.TwoTone.Star,
                        contentDescription = null, // Dekoratives Element
                        tint = if (isStarred) androidx.compose.ui.graphics.Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.3f
                        ),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
