package io.heckel.ntfy.ui

import android.content.Context
import android.graphics.Color
import com.google.android.material.color.MaterialColors
import io.heckel.ntfy.R

class Colors {
    companion object {
        fun primary(context: Context): Int {
            return MaterialColors.getColor(context, R.attr.colorPrimary, Color.GREEN)
        }

        fun onPrimary(context: Context): Int {
            return MaterialColors.getColor(context, R.attr.colorOnPrimary, Color.GREEN)
        }

        fun notificationIcon(context: Context): Int {
            return MaterialColors.getColor(context, R.attr.colorPrimary, Color.GREEN)
        }

        fun linkColor(context: Context): Int {
            return MaterialColors.getColor(context, R.attr.colorPrimary, Color.GREEN)
        }

        fun itemSelectedBackground(context: Context): Int {
            return MaterialColors.getColor(context, R.attr.glassPaneSelected, Color.GRAY)
        }

        // Frosted glass panes (colours from the active theme, see res/values/glass_attrs.xml): translucent over the aurora backdrop
        fun cardBackgroundColor(context: Context): Int {
            return MaterialColors.getColor(context, R.attr.glassPane, Color.WHITE)
        }

        fun cardSelectedBackgroundColor(context: Context): Int {
            return MaterialColors.getColor(context, R.attr.glassPaneSelected, Color.GRAY)
        }

        fun statusBarNormal(context: Context, dynamicColors: Boolean, darkMode: Boolean): Int {
            // Translucent glass bar in all modes; the aurora backdrop tints through it
            return MaterialColors.getColor(context, R.attr.glassBar, Color.WHITE)
        }

        fun shouldUseLightStatusBar(dynamicColors: Boolean, darkMode: Boolean): Boolean {
            // Glass bar is light in light mode, so use dark status bar icons there
            return !darkMode
        }

        fun toolbarTextColor(context: Context, dynamicColors: Boolean, darkMode: Boolean): Int {
            // Glass bar follows the surface, so text is ink-on-light / white-on-dark
            return MaterialColors.getColor(context, R.attr.colorOnSurface, if (darkMode) Color.WHITE else Color.BLACK)
        }

        fun dangerText(context: Context): Int {
            return MaterialColors.getColor(context, R.attr.colorError, Color.RED)
        }

        fun swipeToRefreshColor(context: Context): Int {
            return MaterialColors.getColor(context, R.attr.colorPrimary, Color.GREEN)
        }
    }
}
