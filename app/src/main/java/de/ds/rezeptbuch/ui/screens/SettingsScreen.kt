package de.ds.rezeptbuch.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.ds.rezeptbuch.data.model.Category
import de.ds.rezeptbuch.ui.theme.AppColorScheme
import de.ds.rezeptbuch.ui.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import java.io.InputStream

private enum class SettingsPage {
    MAIN, APPEARANCE, CATEGORIES, IMPORT_EXPORT, ABOUT
}

@Composable
fun SettingsScreen(
    viewModel: RecipeViewModel,
    onImportJsonClick: (String) -> Unit,
    onImportMcbClick: (InputStream) -> Unit,
    onExportJsonClick: suspend () -> String,
    onBackClick: () -> Unit
) {
    val currentPage = remember { mutableStateOf(value = SettingsPage.MAIN) }

    when (currentPage.value) {
        SettingsPage.MAIN -> MainSettingsScreen(
            onBackClick = onBackClick,
            onNavigateToAppearance = { currentPage.value = SettingsPage.APPEARANCE },
            onNavigateToCategories = { currentPage.value = SettingsPage.CATEGORIES },
            onNavigateToImportExport = { currentPage.value = SettingsPage.IMPORT_EXPORT },
            onNavigateToAbout = { currentPage.value = SettingsPage.ABOUT }
        )

        SettingsPage.APPEARANCE -> AppearanceSettingsScreen(
            viewModel = viewModel,
            onBackClick = { currentPage.value = SettingsPage.MAIN }
        )

        SettingsPage.CATEGORIES -> CategorySettingsScreen(
            viewModel = viewModel,
            onBackClick = { currentPage.value = SettingsPage.MAIN }
        )

        SettingsPage.IMPORT_EXPORT -> ImportExportSettingsScreen(
            onImportJsonClick = onImportJsonClick,
            onImportMcbClick = onImportMcbClick,
            onExportJsonClick = onExportJsonClick,
            onBackClick = { currentPage.value = SettingsPage.MAIN }
        )

        SettingsPage.ABOUT -> AboutSettingsScreen(
            onBackClick = { currentPage.value = SettingsPage.MAIN }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainSettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SettingsItem(
                    title = "Aussehen",
                    subtitle = "Ändern Sie das Farbschema",
                    onClick = onNavigateToAppearance
                )
            }
            item {
                SettingsItem(
                    title = "Kategorien",
                    subtitle = "Bearbeiten Sie die Kategorien",
                    onClick = onNavigateToCategories
                )
            }
            item {
                SettingsItem(
                    title = "Importieren/Exportieren",
                    subtitle = "Rezepte sichern oder wiederherstellen",
                    onClick = onNavigateToImportExport
                )
            }
            item {
                SettingsItem(
                    title = "Über diese App",
                    subtitle = "Erfahren Sie mehr über Rezeptbuch",
                    onClick = onNavigateToAbout
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySettingsScreen(
    viewModel: RecipeViewModel,
    onBackClick: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    var newCategoryName by remember { mutableStateOf("") }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var editedName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kategorien") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Neue Kategorie") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        viewModel.addNewCategory(newCategoryName)
                        newCategoryName = ""
                    },
                    enabled = newCategoryName.isNotBlank()
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Hinzufügen")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    if (editingCategory == category) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = {
                                viewModel.renameCategory(category.name, editedName)
                                editingCategory = null
                            }) {
                                Icon(Icons.Rounded.Check, contentDescription = "Speichern")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category.name, style = MaterialTheme.typography.bodyLarge)
                            Row {
                                IconButton(onClick = {
                                    editingCategory = category
                                    editedName = category.name
                                }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Bearbeiten")
                                }
                                IconButton(onClick = { viewModel.deleteCategory(category.name) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Löschen")
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSettingsScreen(
    viewModel: RecipeViewModel,
    onBackClick: () -> Unit
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val appColorScheme by viewModel.appColorScheme.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val isGridLayout by viewModel.isGridLayout.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aussehen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Wählen Sie ein Farbschema",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(AppColorScheme.entries) { scheme ->
                    val color = when (scheme) {
                        AppColorScheme.GREEN -> Color(0xFF4CAF50)
                        AppColorScheme.BLUE -> Color(0xFF2196F3)
                        AppColorScheme.RED -> Color(0xFF410009)
                        AppColorScheme.ORANGE -> Color(0xFFFF9800)
                        AppColorScheme.PURPLE -> Color(0xFF9C27B0)
                        AppColorScheme.PINK -> Color(0xFFE91E63)
                        AppColorScheme.TEAL -> Color(0xFF009688)
                        AppColorScheme.BROWN -> Color(0xFF795548)
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { viewModel.updateColorScheme(scheme) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (appColorScheme == scheme) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            SettingSwitchItem(
                title = "Dunkler Modus",
                checked = isDarkTheme == true,
                onCheckedChange = { viewModel.toggleTheme(it) }
            )

            SettingSwitchItem(
                title = "Halten Sie den Bildschirm eingeschaltet, wenn Sie Rezepte anzeigen",
                checked = keepScreenOn,
                onCheckedChange = { viewModel.toggleKeepScreenOn() }
            )

            SettingSwitchItem(
                title = "Zweispaltiges Layout in der Rezeptliste",
                checked = isGridLayout,
                onCheckedChange = { viewModel.toggleGridLayout() }
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportExportSettingsScreen(
    onImportJsonClick: (String) -> Unit,
    onImportMcbClick: (InputStream) -> Unit,
    onExportJsonClick: suspend () -> String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                } catch (_: Exception) {
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val jsonString =
                        inputStream.bufferedReader().use { reader -> reader.readText() }
                    onImportJsonClick(jsonString)
                }
            } catch (_: Exception) {
            }
        }
    }

    val mcbLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.let { inputStream ->
                    onImportMcbClick(inputStream)
                }
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importieren/Exportieren") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SettingsItem(
                title = "Backup erstellen",
                subtitle = "Rezepte in einer JSON-Datei speichern",
                onClick = { createDocumentLauncher.launch("rezepte_backup.json") }
            )
            SettingsItem(
                title = "Backup wiederherstellen",
                subtitle = "Rezepte aus einer JSON-Datei laden",
                onClick = { restoreLauncher.launch("application/json") }
            )
            SettingsItem(
                title = "My Cookbook (.mcb) importieren",
                subtitle = "Rezepte aus einer My Cookbook Datei importieren",
                onClick = { mcbLauncher.launch("*/*") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0"
    val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        packageInfo?.longVersionCode?.toString() ?: "1"
    } else {
        @Suppress("DEPRECATION")
        packageInfo?.versionCode?.toString() ?: "1"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Über Mein Rezeptbuch") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Mein Rezeptbuch",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.size(16.dp))

            Text(
                "Entwickelt von: Dietmar Schröer",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                "Version: $versionName",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                "Build-Nummer: $versionCode",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                "Private Rezeptsammlung",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
