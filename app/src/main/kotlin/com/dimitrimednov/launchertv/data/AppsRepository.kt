package com.dimitrimednov.launchertv.data

import androidx.core.content.edit
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Una app instalada que se puede abrir desde el inicio. */
data class LauncherApp(
    val packageName: String,
    val name: String,
    val component: ComponentName,
    val brand: Brand,
)

/** Todas las apps con su orden guardado, separadas en visibles y ocultas. */
data class AppList(val visible: List<LauncherApp>, val hidden: List<LauncherApp>)

/**
 * Lee las apps de tele instaladas (las que tienen `LEANBACK_LAUNCHER`) y guarda el orden
 * y las ocultas en SharedPreferences.
 */
class AppsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("apps", Context.MODE_PRIVATE)

    suspend fun load(): AppList = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val installed = pm.queryIntentActivities(query, PackageManager.MATCH_ALL)
            .map { it.activityInfo }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map { info ->
                val known = KnownApps.firstOrNull { it.packageName == info.packageName }
                LauncherApp(
                    packageName = info.packageName,
                    name = known?.name ?: info.loadLabel(pm).toString(),
                    component = ComponentName(info.packageName, info.name),
                    brand = known?.brand ?: DefaultBrand,
                )
            }
            .associateBy { it.packageName }

        val ordered = defaultOrder(installed.values).let { fallback ->
            val saved = savedOrder().filter { it in installed }
            saved + fallback.filter { it !in saved }
        }.map { installed.getValue(it) }

        val hidden = hiddenPackages()
        AppList(
            visible = ordered.filter { it.packageName !in hidden },
            hidden = ordered.filter { it.packageName in hidden },
        )
    }

    fun saveOrder(packages: List<String>) {
        val rest = savedOrder().filter { it !in packages }
        prefs.edit { putString(KEY_ORDER, (packages + rest).joinToString(",")) }
    }

    fun setHidden(packageName: String, hidden: Boolean) {
        val current = hiddenPackages()
        val next = if (hidden) current + packageName else current - packageName
        prefs.edit { putStringSet(KEY_HIDDEN, next) }
    }

    private fun savedOrder(): List<String> =
        prefs.getString(KEY_ORDER, null)?.split(",")?.filter { it.isNotBlank() }.orEmpty()

    private fun hiddenPackages(): Set<String> =
        prefs.getStringSet(KEY_HIDDEN, null) ?: HiddenByDefault

    private fun defaultOrder(apps: Collection<LauncherApp>): List<String> {
        val known = KnownApps.map { it.packageName }.filter { pkg -> apps.any { it.packageName == pkg } }
        val others = apps.filter { it.packageName !in known }.sortedBy { it.name.lowercase() }.map { it.packageName }
        return known + others
    }

    private companion object {
        const val KEY_ORDER = "order"
        const val KEY_HIDDEN = "hidden"
    }
}
