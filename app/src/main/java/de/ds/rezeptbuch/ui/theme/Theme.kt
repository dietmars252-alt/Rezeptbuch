package de.ds.rezeptbuch.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class AppColorScheme {
    GREEN, BLUE, RED, PURPLE
}

private fun getLightColorScheme(base: AppColorScheme): ColorScheme {
    return when (base) {
        AppColorScheme.GREEN -> lightColorScheme(
            primary = PrimaryLight,
            onPrimary = OnPrimaryLight,
            primaryContainer = PrimaryContainerLight,
            onPrimaryContainer = OnPrimaryContainerLight,
            secondary = SecondaryLight,
            onSecondary = OnSecondaryLight,
            secondaryContainer = SecondaryContainerLight,
            onSecondaryContainer = OnSecondaryContainerLight,
            tertiary = TertiaryLight,
            onTertiary = OnTertiaryLight,
            tertiaryContainer = TertiaryContainerLight,
            onTertiaryContainer = OnTertiaryContainerLight
        )
        AppColorScheme.BLUE -> lightColorScheme(
            primary = BluePrimaryLight,
            onPrimary = BlueOnPrimaryLight,
            primaryContainer = BlueContainerLight,
            onPrimaryContainer = BlueOnContainerLight
        )
        AppColorScheme.RED -> lightColorScheme(
            primary = RedPrimaryLight,
            onPrimary = RedOnPrimaryLight,
            primaryContainer = RedContainerLight,
            onPrimaryContainer = RedOnContainerLight
        )
        AppColorScheme.PURPLE -> lightColorScheme(
            primary = PurplePrimaryLight,
            onPrimary = PurpleOnPrimaryLight,
            primaryContainer = PurpleContainerLight,
            onPrimaryContainer = PurpleOnContainerLight
        )
    }
}

private fun getDarkColorScheme(base: AppColorScheme): ColorScheme {
    return when (base) {
        AppColorScheme.GREEN -> darkColorScheme(
            primary = PrimaryDark,
            onPrimary = OnPrimaryDark,
            primaryContainer = PrimaryContainerDark,
            onPrimaryContainer = OnPrimaryContainerDark,
            secondary = SecondaryDark,
            onSecondary = OnSecondaryDark,
            secondaryContainer = SecondaryContainerDark,
            onSecondaryContainer = OnSecondaryContainerDark,
            tertiary = TertiaryDark,
            onTertiary = OnTertiaryDark,
            tertiaryContainer = TertiaryContainerDark,
            onTertiaryContainer = OnTertiaryContainerDark
        )
        AppColorScheme.BLUE -> darkColorScheme(
            primary = BluePrimaryDark,
            onPrimary = BlueOnPrimaryDark,
            primaryContainer = BlueContainerDark,
            onPrimaryContainer = BlueOnContainerDark
        )
        AppColorScheme.RED -> darkColorScheme(
            primary = RedPrimaryDark,
            onPrimary = RedOnPrimaryDark,
            primaryContainer = RedContainerDark,
            onPrimaryContainer = RedOnContainerDark
        )
        AppColorScheme.PURPLE -> darkColorScheme(
            primary = PurplePrimaryDark,
            onPrimary = PurpleOnPrimaryDark,
            primaryContainer = PurpleContainerDark,
            onPrimaryContainer = PurpleOnContainerDark
        )
    }
}

@Composable
fun RezeptbuchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorSchemeSelection: AppColorScheme = AppColorScheme.GREEN,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> getDarkColorScheme(colorSchemeSelection)
        else -> getLightColorScheme(colorSchemeSelection)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
