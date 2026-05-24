package de.ds.rezeptbuch.data.remote

import android.content.Context
import android.util.Xml
import de.ds.rezeptbuch.data.model.Category
import de.ds.rezeptbuch.data.model.Ingredient
import de.ds.rezeptbuch.data.model.Recipe
import de.ds.rezeptbuch.data.repository.RecipeRepository
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.regex.Pattern
import java.util.zip.ZipInputStream

class McbImportService(
    private val context: Context,
    private val repository: RecipeRepository
) {

    suspend fun importMcb(inputStream: InputStream) {
        val tempDir = File(context.cacheDir, "mcb_import_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        try {
            unzip(inputStream, tempDir)
            // Suchen nach einer beliebigen XML-Datei im entpackten Verzeichnis
            val xmlFile = tempDir.walkTopDown().find { it.extension.lowercase() == "xml" }
            if (xmlFile != null && xmlFile.exists()) {
                android.util.Log.d("McbImportService", "Found XML file: ${xmlFile.name}")
                parseAndImportXml(xmlFile, tempDir)
            } else {
                android.util.Log.e("McbImportService", "No XML file found in MCB archive")
            }
        } catch (e: Exception) {
            android.util.Log.e("McbImportService", "Error during MCB import", e)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun unzip(inputStream: InputStream, targetDir: File) {
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val file = File(targetDir, entry.name)
                // Sicherheitshalber prüfen, ob die Datei im Zielverzeichnis bleibt (Zip Slip prevention)
                if (!file.canonicalPath.startsWith(targetDir.canonicalPath)) {
                    android.util.Log.e("McbImportService", "Skipping unsafe zip entry: ${entry.name}")
                    entry = zis.nextEntry
                    continue
                }

                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { zis.copyTo(it) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private suspend fun parseAndImportXml(xmlFile: File, tempDir: File) {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(xmlFile.inputStream(), null)

        var eventType = parser.eventType
        var currentRecipe: McbRecipe? = null
        var recipeCount = 0

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "recipe") {
                        currentRecipe = McbRecipe()
                    } else if (currentRecipe != null) {
                        try {
                            when (name) {
                                "title" -> currentRecipe.title = parser.nextText()
                                "preptime" -> currentRecipe.preptime = parseTime(parser.nextText())
                                "cooktime" -> currentRecipe.cooktime = parseTime(parser.nextText())
                                "quantity" -> currentRecipe.quantity = parser.nextText().toIntOrNull() ?: 1
                                "category" -> currentRecipe.categories.add(parser.nextText())
                                "comments" -> currentRecipe.comments = parser.nextText()
                                "rating" -> currentRecipe.rating = parser.nextText().toIntOrNull() ?: 0
                                "imagepath" -> {
                                    currentRecipe.imagePath = parser.nextText()
                                    android.util.Log.d("McbImportService", "Parsed imagepath: ${currentRecipe.imagePath}")
                                }
                                "ingredient" -> currentRecipe.ingredients.addAll(parseListItems(parser))
                                "recipetext" -> currentRecipe.instructions.addAll(parseListItems(parser))
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("McbImportService", "Error parsing tag $name", e)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name == "recipe" && currentRecipe != null) {
                        try {
                            importRecipe(currentRecipe, tempDir)
                            recipeCount++
                        } catch (e: Exception) {
                            android.util.Log.e("McbImportService", "Error importing recipe ${currentRecipe.title}", e)
                        }
                        currentRecipe = null
                    }
                }
            }
            eventType = parser.next()
        }
        android.util.Log.d("McbImportService", "Imported $recipeCount recipes")
    }

    private fun parseListItems(parser: XmlPullParser): List<String> {
        val items = mutableListOf<String>()
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && (parser.name == "ingredient" || parser.name == "recipetext"))) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "li") {
                items.add(parser.nextText())
            }
            eventType = parser.next()
        }
        return items
    }

    private suspend fun importRecipe(mcbRecipe: McbRecipe, tempDir: File) {
        android.util.Log.d("McbImportService", "Importing recipe: ${mcbRecipe.title}")
        
        // Prüfen, ob das Rezept bereits existiert
        val existingRecipe = repository.getRecipeByTitle(mcbRecipe.title)
        
        // Save image if exists
        var finalImagePath: String? = existingRecipe?.recipe?.bildpfad
        
        if (!mcbRecipe.imagePath.isNullOrBlank()) {
            val sourceImage = File(tempDir, mcbRecipe.imagePath!!)
            android.util.Log.d("McbImportService", "Recipe has imagePath: ${mcbRecipe.imagePath}. Checking at: ${sourceImage.absolutePath}")
            
            var imageToCopy: File? = if (sourceImage.exists()) sourceImage else null
            
            if (imageToCopy == null) {
                // Alternativ-Suche
                val fileName = sourceImage.name
                imageToCopy = tempDir.walkTopDown().find { it.name.lowercase() == fileName.lowercase() }
                if (imageToCopy != null) {
                    android.util.Log.i("McbImportService", "Found image file at alternative location: ${imageToCopy.absolutePath}")
                }
            }

            if (imageToCopy != null) {
                try {
                    val destDir = File(context.filesDir, "recipe_images")
                    destDir.mkdirs()
                    val destFile = File(destDir, "img_${System.currentTimeMillis()}_${imageToCopy.name}")
                    imageToCopy.copyTo(destFile, overwrite = true)
                    finalImagePath = destFile.absolutePath
                    android.util.Log.d("McbImportService", "Image successfully copied to: $finalImagePath")
                } catch (e: Exception) {
                    android.util.Log.e("McbImportService", "Error copying image", e)
                }
            } else {
                android.util.Log.w("McbImportService", "Image file not found for ${mcbRecipe.title}")
            }
        }

        val recipe = Recipe(
            id = existingRecipe?.recipe?.id ?: 0,
            titel = mcbRecipe.title,
            kategorien = mcbRecipe.categories,
            portionen = mcbRecipe.quantity,
            anweisungen = mcbRecipe.instructions,
            bewertung = mcbRecipe.rating,
            arbeitszeit = mcbRecipe.preptime,
            kochzeit = mcbRecipe.cooktime,
            notizen = mcbRecipe.comments,
            bildpfad = finalImagePath
        )

        // Add categories to database
        if (mcbRecipe.categories.isNotEmpty()) {
            repository.insertCategories(mcbRecipe.categories.map { Category(it) })
        }

        val ingredients = mcbRecipe.ingredients.mapNotNull { 
            parseIngredientLine(it)
        }

        if (existingRecipe != null) {
            android.util.Log.d("McbImportService", "Updating existing recipe: ${mcbRecipe.title}")
            repository.updateRecipeWithIngredients(recipe, ingredients)
        } else {
            android.util.Log.d("McbImportService", "Inserting new recipe: ${mcbRecipe.title}")
            repository.insertRecipeWithIngredients(recipe, ingredients)
        }
    }

    private fun parseTime(timeStr: String): Int? {
        val matcher = Pattern.compile("(\\d+)").matcher(timeStr)
        return if (matcher.find()) matcher.group(1)?.toIntOrNull() else null
    }

    private fun parseIngredientLine(line: String): Ingredient? {
        if (line.isBlank()) return null
        val pattern = Pattern.compile("^([\\d,./]+)?\\s*([a-zA-ZäöüÄÖÜß.]+)?\\s*(.*)$")
        val matcher = pattern.matcher(line.trim())
        if (matcher.find()) {
            val mengeStr = matcher.group(1)?.replace(",", ".")
            var einheit = matcher.group(2) ?: ""
            var name = matcher.group(3) ?: ""
            var menge = try {
                if (mengeStr == null) 0.0 else parseMenge(mengeStr)
            } catch (_: Exception) { 0.0 }
            if (menge > 0 && (einheit.equals("l", ignoreCase = true) || einheit.equals("liter", ignoreCase = true))) {
                if (menge < 1.0) { menge *= 1000; einheit = "ml" }
            }
            if (mengeStr != null && name.isBlank()) { name = einheit; einheit = "" }
            else if (mengeStr == null) { name = (einheit + " " + name).trim(); einheit = "" }
            return Ingredient(rezeptId = 0, name = name, menge = menge, einheit = einheit)
        }
        return null
    }

    private fun parseMenge(mengeStr: String): Double {
        val trimmed = mengeStr.trim().replace(",", ".")
        if (trimmed.isBlank()) return 0.0
        
        return try {
            if (trimmed.contains("/")) {
                val parts = trimmed.split("/")
                if (parts.size == 2) {
                    val numerator = parts[0].trim().toDouble()
                    val denominator = parts[1].trim().toDouble()
                    if (denominator != 0.0) numerator / denominator else 1.0
                } else 1.0
            } else {
                trimmed.toDouble()
            }
        } catch (_: Exception) {
            android.util.Log.w("McbImportService", "Could not parse quantity: $mengeStr")
            1.0
        }
    }

    private class McbRecipe {
        var title: String = ""
        var preptime: Int? = null
        var cooktime: Int? = null
        var quantity: Int = 1
        var categories = mutableListOf<String>()
        var comments: String = ""
        var rating: Int = 0
        var imagePath: String? = null
        var ingredients = mutableListOf<String>()
        var instructions = mutableListOf<String>()
    }
}
