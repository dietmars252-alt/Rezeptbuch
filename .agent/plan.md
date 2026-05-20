# Project Plan

The user wants to add editing and deleting features for recipes in the "Rezeptbuch" app. This involves updating the UI to provide edit/delete triggers (e.g., in the detail screen) and updating the data layer to handle these operations.

## Project Brief

# Projekt-Brief: Rezeptbuch

**Rezeptbuch** ist eine moderne Android-App zur digitalen Verwaltung kulinarischer Rezepte. Die App ermöglicht es Nutzern, ihre eigene Sammlung zu pflegen, neue Rezepte strukturiert zu erfassen sowie bestehende Einträge flexibel anzupassen oder zu entfernen. Die Benutzeroberfläche ist vollständig in Deutsch gehalten und folgt den Material Design 3 Prinzipien.

## Features
*   **Rezept-Übersicht & Suche**: Eine zentrale, durchsuchbare Liste aller Rezepte mit Filteroptionen für Kategorien, um gewünschte Gerichte schnell zu finden.
*   **Rezept-Erstellung (FAB)**: Über einen Floating Action Button erreichbares Formular zur Eingabe neuer Rezepte (Titel, Kategorie, Portionen, Zutaten und Anleitung).
*   **Rezept-Verwaltung (Anzeigen, Bearbeiten & Löschen)**: Detaillierte Ansicht mit Funktionen zur nachträglichen Bearbeitung aller Felder sowie einer Löschfunktion inklusive Sicherheitsabfrage.
*   **Adaptives Material 3 Design**: Optimierte Darstellung für unterschiedliche Formfaktoren (Smartphone, Tablet, Foldables) durch adaptive Layout-Strategien (z. B. List-Detail-Ansicht).

## High-Level Technical Stack
*   **Kotlin & Jetpack Compose**: Modernes UI-Toolkit für eine deklarative Oberflächengestaltung mit **Material Design 3**.
*   **Jetpack Navigation 3**: Zustandsgesteuerte Navigation innerhalb der App.
*   **Compose Material Adaptive Library**: Implementierung von responsiven Layouts für eine nahtlose Nutzung auf verschiedenen Gerätegrößen.
*   **Room Persistence Library**: Lokale Speicherung und Verwaltung der Rezeptdaten (notwendig für die Funktionen Hinzufügen, Bearbeiten und Löschen).
*   **Coroutines**: Effiziente asynchrone Verarbeitung für Datenbankzugriffe und flüssige UI-Interaktionen.

## Implementation Steps
**Total Duration:** 4h 9m 2s

### Task_1_DataModelRepository: Define the data models (Recipe, Ingredient, Category) and set up a Room database or a Repository with mock data inspired by the provided website content. Ensure German strings are used for entity properties.
- **Status:** COMPLETED
- **Updates:** Established Room database with German-named entities (Recipe, Ingredient, Category). Implemented Repository with mock data seeding for recipes like Apfelkuchen and Chili con Carne. Verified successful build.
- **Acceptance Criteria:**
  - Data models represent recipes, ingredients, and categories correctly
  - Repository provides a list of recipes and filterable categories
  - Room database or Mock data source is functional
- **Duration:** 10m

### Task_2_AdaptiveListUI: Implement the main adaptive UI using Compose Material Adaptive List-Detail-Pane. Create the Recipe List screen with a search bar and category filters in German.
- **Status:** COMPLETED
- **Updates:** Implemented an adaptive UI using ListDetailPaneScaffold. Created a searchable and filterable recipe list and a detail view in German. Integrated ViewModel for state management. Verified on device.
- **Acceptance Criteria:**
  - App uses ListDetailPaneScaffold for adaptive layouts
  - Recipe list is searchable and filterable by category
  - UI follows Material Design 3 and is in German
- **Duration:** 3m 32s

### Task_3_RecipeDetailFavorites: Develop the detailed recipe view including ingredients, preparation steps, and a portion calculator. Implement the favorites system using Room for persistence.
- **Status:** COMPLETED
- **Updates:** Implemented a dynamic portion calculator that scales ingredient quantities. Enhanced the recipe detail view with numbered preparation steps. Integrated a persistent favorites system with a dedicated filter toggle in the recipe list. Polished UI with M3 components.
- **Acceptance Criteria:**
  - Recipe detail screen displays all necessary information
  - Portion calculator updates ingredient quantities correctly
  - Users can mark/unmark favorites and view them in a filtered list
- **Duration:** 3m 14s

### Task_4_BrandingAndTheming: Apply a vibrant Material 3 color scheme (Light/Dark), implement full Edge-to-Edge display, and create an adaptive app icon matching the 'Rezeptbuch' theme.
- **Status:** COMPLETED
- **Updates:** Implemented a vibrant M3 color scheme with fresh greens and warm oranges. Enabled full edge-to-edge display with proper inset handling. Created an adaptive app icon featuring a recipe book and chef's hat. Supported both light and dark modes.
- **Acceptance Criteria:**
  - App uses a vibrant M3 color system with Material Color Utilities
  - Edge-to-edge display is enabled and functional
  - Adaptive app icon is present and matches the theme
- **Duration:** 3m 30s

### Task_5_FinalRunVerify: Perform a final run of the application to verify stability, alignment with the 'Rezeptbuch' requirements, and ensure no crashes occur. Confirm German UI consistency.
- **Status:** COMPLETED
- **Updates:** The final verification was successful. The app is stable, functional, and meets all requirements. No crashes were detected during navigation or search. The German UI is consistent, and the portion calculator and favorites system work as expected. Material Design 3 guidelines are followed with a vibrant theme and adaptive icon.
- **Acceptance Criteria:**
  - Project builds successfully
  - App does not crash during navigation or search
  - All existing tests pass
  - UI matches Material Design 3 guidelines and user requirements
- **Duration:** 3h 8m 21s

### Task_6_RecipeEntryUI: Implement a Floating Action Button (FAB) on the recipe list and create a new screen or form for entering recipe details (Titel, Kategorie, Portionen, Zutaten, Anleitung) in German.
- **Status:** COMPLETED
- **Updates:** Implemented a Floating Action Button (FAB) on the recipe list that navigates to a new RecipeEntryScreen. The form allows for entering all recipe details (Title, Category, Portions, Ingredients, Instructions) in German. Integrated state management in RecipeViewModel and verified the save logic to Room database.
- **Acceptance Criteria:**
  - FAB is visible and navigates to the entry form
  - Entry form allows input of all recipe fields
  - UI follows Material Design 3 guidelines
- **Duration:** 2m 46s

### Task_7_PersistenceAndFinalVerify: Integrate the recipe entry form with the Room database and Repository to persist new recipes. Perform a final run and verify application stability and requirement alignment.
- **Status:** COMPLETED
- **Updates:** The recipe creation feature is fully implemented and verified. Users can add new recipes with all details (title, category, portions, ingredients, steps) via a FAB. The data is persisted in the Room database and appears immediately in the list. The app is stable, follows Material 3 guidelines, and is fully localized in German. Final verification by critic_agent was successful.
- **Acceptance Criteria:**
  - New recipes are saved correctly and appear in the list
  - App does not crash during recipe creation or saving
  - Build pass and all existing tests pass
- **Duration:** 26m 26s

### Task_8_EditDeleteFunctionality: Implement editing and deleting functionality for recipes. Add edit/delete triggers to the detail screen, update the Room data layer (DAO/Repository), and adapt the entry form to support editing existing recipes with pre-filled data and a deletion confirmation dialog.
- **Status:** COMPLETED
- **Updates:** Implemented full editing and deleting capabilities. Added edit/delete actions to the detail screen with a confirmation dialog for deletion. Enhanced the recipe form to support pre-filling for existing recipes. Updated the Room layer to handle atomic updates and cascading deletes for ingredients. Verified smooth navigation and data consistency.
- **Acceptance Criteria:**
  - Recipes can be updated and deleted successfully
  - Confirmation dialog prevents accidental deletion
  - UI reflects changes immediately
  - German UI consistency is maintained
- **Duration:** 5m 17s

### Task_9_FinalRunVerifyEditDelete: Perform a final run of the application to verify stability and alignment with the editing/deleting requirements. Ensure the app adheres to Material Design 3 and no crashes occur during management operations.
- **Status:** COMPLETED
- **Updates:** The final verification was successful. The editing and deleting features are stable and work as expected. The app correctly updates and deletes records in the Room database, handles navigation transitions, and uses Material 3 components (icons, dialogs) in German. No crashes were detected.
- **Acceptance Criteria:**
  - Project builds successfully
  - App does not crash during edit or delete operations
  - All existing tests pass
  - UI matches Material Design 3 guidelines and user requirements
- **Duration:** 5m 56s

