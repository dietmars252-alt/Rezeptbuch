package de.ds.rezeptbuch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.ds.rezeptbuch.data.local.AppDatabase
import de.ds.rezeptbuch.data.repository.RecipeRepository
import de.ds.rezeptbuch.ui.screens.MainAdaptiveScreen
import de.ds.rezeptbuch.ui.theme.RezeptbuchTheme
import de.ds.rezeptbuch.ui.viewmodel.RecipeViewModel
import de.ds.rezeptbuch.ui.viewmodel.RecipeViewModelFactory

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { RecipeRepository(database.recipeDao()) }
    private val viewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkThemeOverride by viewModel.isDarkTheme.collectAsState()
            val colorSchemeSelection by viewModel.appColorScheme.collectAsState()
            val darkTheme = isDarkThemeOverride ?: isSystemInDarkTheme()

            RezeptbuchTheme(
                darkTheme = darkTheme,
                colorSchemeSelection = colorSchemeSelection
            ) {
                MainAdaptiveScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAdaptivePreview() {
    RezeptbuchTheme {
        // Preview with mock data or empty state
    }
}
