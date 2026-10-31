package com.dimitrimednov.launchertv.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Un fondo: foto o video. `file == null` es el degradado de anochecer que viene dentro de la app. */
data class BackgroundItem(val name: String, val isVideo: Boolean, val file: File?) {
    val id: String get() = file?.path ?: "incluido"

    companion object {
        val Builtin = BackgroundItem("Anochecer", isVideo = false, file = null)
    }
}

/**
 * Fondos propios en la carpeta de la app en la tele (`Android/data/<app>/files/fondos`).
 * Se copian con `scripts/fondos.sh copiar`; así las fotos y videos no viven en el repo.
 */
class BackgroundsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("fondos", Context.MODE_PRIVATE)
    private var cached: List<BackgroundItem> = emptyList()

    /**
     * Se llama cada vez que se vuelve al inicio. Solo se revuelve de nuevo si cambiaron los
     * archivos; si no, se devuelve la misma lista para no saltar de fondo.
     */
    suspend fun load(): List<BackgroundItem> = withContext(Dispatchers.IO) {
        val dir = context.getExternalFilesDir(FOLDER) ?: return@withContext emptyList()
        val found = dir.listFiles().orEmpty().mapNotNull { file ->
            when (file.extension.lowercase()) {
                in VIDEO -> BackgroundItem(displayName(file), isVideo = true, file = file)
                in PHOTO -> BackgroundItem(displayName(file), isVideo = false, file = file)
                else -> null
            }
        }
        if (found.map { it.id }.toSet() != cached.map { it.id }.toSet()) cached = avoidLastFirst(found.shuffled())
        cached
    }

    /** Revuelta al azar, pero sin empezar con el mismo fondo que la vez anterior. */
    private fun avoidLastFirst(items: List<BackgroundItem>): List<BackgroundItem> {
        val sorted = if (items.size > 1 && items.first().id == prefs.getString(KEY_FIRST, null)) items.drop(1) + items.first() else items
        sorted.firstOrNull()?.let { prefs.edit { putString(KEY_FIRST, it.id) } }
        return sorted
    }

    /** "gato-mirando-la-lluvia.mp4" → "Gato mirando la lluvia". */
    private fun displayName(file: File) =
        file.nameWithoutExtension.replace('-', ' ').replace('_', ' ').trim().replaceFirstChar { it.uppercase() }

    private companion object {
        const val FOLDER = "fondos"
        const val KEY_FIRST = "primero"
        val VIDEO = setOf("mp4", "mkv", "webm")
        val PHOTO = setOf("jpg", "jpeg", "png", "webp")
    }
}
