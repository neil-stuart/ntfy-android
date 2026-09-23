package io.heckel.ntfy.ui.theme

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import io.heckel.ntfy.R
import io.heckel.ntfy.db.Repository
import java.util.WeakHashMap

/**
 * Selectable colour themes. Ocean, Pepper, Cherry and Tokyo Night are ported from Nocean
 * (https://github.com/AurielSolaris/Nocean, MIT). Cherry is light-only and Tokyo Night is
 * dark-only, so they override the user's light/dark choice while selected.
 */
enum class HomeTheme(
    @StringRes val label: Int,
    @StyleRes val overlay: Int,
    val swatch: Int, // Preview dot colour in the theme picker
    val forceLight: Boolean = false,
    val forceDark: Boolean = false,
) {
    HAWKSFIELD(R.string.theme_hawksfield, R.style.ThemeOverlay_Home_Hawksfield, 0xFF78716C.toInt()),
    OCEAN(R.string.theme_ocean, R.style.ThemeOverlay_Home_Ocean, 0xFF0F62FE.toInt()),
    PEPPER(R.string.theme_pepper, R.style.ThemeOverlay_Home_Pepper, 0xFFE90111.toInt()),
    CHERRY(R.string.theme_cherry, R.style.ThemeOverlay_Home_Cherry, 0xFFE8367E.toInt(), forceLight = true),
    TOKYO(R.string.theme_tokyo, R.style.ThemeOverlay_Home_Tokyo, 0xFF7AA2F7.toInt(), forceDark = true);

    val followsMode: Boolean get() = !forceLight && !forceDark
}

object ThemeManager {
    private const val PREFS = "home_theme"
    private const val KEY_THEME = "theme"

    /** Bumped on every theme change; activities created under an older version recreate on resume. */
    private var version = 0
    private val appliedVersion = WeakHashMap<Activity, Int>()

    fun current(context: Context): HomeTheme {
        val name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_THEME, null)
        return HomeTheme.entries.firstOrNull { it.name == name } ?: HomeTheme.HAWKSFIELD
    }

    fun select(context: Context, theme: HomeTheme) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_THEME, theme.name).apply()
        version++
        applyNightMode(context)
    }

    /** The night mode actually used: a single-mode theme wins over the user's light/dark setting. */
    fun effectiveNightMode(context: Context, userMode: Int): Int {
        val theme = current(context)
        return when {
            theme.forceDark -> AppCompatDelegate.MODE_NIGHT_YES
            theme.forceLight -> AppCompatDelegate.MODE_NIGHT_NO
            else -> userMode
        }
    }

    fun applyNightMode(context: Context) {
        val userMode = Repository.getInstance(context).getDarkMode()
        AppCompatDelegate.setDefaultNightMode(effectiveNightMode(context, userMode))
    }

    fun install(application: Application) {
        applyNightMode(application)
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            // Runs inside super.onCreate(), before the activities call setContentView()
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity !is AppCompatActivity) return
                // Dynamic (wallpaper) colours replace the palette; the glass roles keep Hawksfield defaults
                if (!Repository.getInstance(activity).getDynamicColorsEnabled()) {
                    activity.theme.applyStyle(current(activity).overlay, true)
                }
                activity.window.setBackgroundDrawable(AuroraDrawable(activity))
                appliedVersion[activity] = version
            }

            override fun onActivityResumed(activity: Activity) {
                val applied = appliedVersion[activity] ?: return
                if (applied != version) activity.recreate()
            }

            override fun onActivityDestroyed(activity: Activity) {
                appliedVersion.remove(activity)
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }
}
