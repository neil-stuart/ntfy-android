package io.heckel.ntfy.ui.theme

import android.animation.ArgbEvaluator
import android.app.Activity
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.color.MaterialColors
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur
import io.heckel.ntfy.R

/**
 * The floating glass footer (layout/view_glass_footer.xml): Notifications · + · Settings.
 *
 * The footer is a shared element between the topic list and Settings, so it stays put while the
 * page content crossfades underneath it; switching tabs slides the active pill across and reveals
 * the new tab's label, like a native navigation bar.
 */
class GlassFooter private constructor(
    private val activity: Activity,
    val view: View,
    initial: Tab,
) {
    enum class Tab { NOTIFICATIONS, SETTINGS }

    private inner class TabViews(tabId: Int, iconId: Int, labelId: Int) {
        val tab: View = view.findViewById(tabId)
        val icon: ImageView = view.findViewById(iconId)
        val label: TextView = view.findViewById(labelId)

        fun tint(color: Int) {
            icon.setColorFilter(color)
            label.setTextColor(color)
        }
    }

    private val row = view.findViewById<GlassFooterRow>(R.id.glass_footer_row)
    private val onPrimary = MaterialColors.getColor(view, R.attr.colorOnPrimary)
    private val onSurface = MaterialColors.getColor(view, R.attr.colorOnSurface)
    private val tabs = mapOf(
        Tab.NOTIFICATIONS to TabViews(R.id.glass_footer_notifications, R.id.glass_footer_notifications_icon, R.id.glass_footer_notifications_label),
        Tab.SETTINGS to TabViews(R.id.glass_footer_settings, R.id.glass_footer_settings_icon, R.id.glass_footer_settings_label),
    )

    var selected: Tab = initial
        private set

    init {
        val density = activity.resources.displayMetrics.density
        val radius = 28 * density
        val bar = MaterialColors.getColor(view, R.attr.glassBar)
        val blurView = view.findViewById<BlurView>(R.id.glass_footer_blur)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Real frosted glass: blur whatever scrolls behind the footer, then tint it
            val root = activity.findViewById<ViewGroup>(android.R.id.content)
            blurView.setupWith(root, RenderEffectBlur())
                .setFrameClearDrawable(activity.window.decorView.background)
                .setBlurRadius(22f)
                .setOverlayColor(bar)
            view.foreground = GlassDrawable(activity, radius, fillColor = 0) // just the rim
        } else {
            // No backdrop blur before Android 12: a denser tint keeps text legible
            blurView.isVisible = false
            val alpha = ((android.graphics.Color.alpha(bar) / 255f) + 0.2f).coerceAtMost(0.94f)
            view.background = GlassDrawable(activity, radius, ColorUtils.setAlphaComponent(bar, (alpha * 255).toInt()))
        }
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
        view.clipToOutline = true

        // Float above the navigation bar. fitsSystemWindows stops a CoordinatorLayout parent from
        // insetting the footer as well, so it sits at the same height on every screen.
        view.fitsSystemWindows = true
        val baseMargin = (20 * density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = baseMargin + systemBars.bottom }
            insets
        }
        // Fixed width (the widest tab state), like a native bottom bar: switching tabs only moves
        // things inside the footer, so its position never shifts while pages transition
        view.updateLayoutParams<ViewGroup.LayoutParams> { width = Tab.entries.maxOf { measuredWidthFor(it) } }
        applyTab(initial, animate = false)
    }

    private fun measuredWidthFor(tab: Tab): Int {
        applyTab(tab, animate = false)
        val unspecified = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(unspecified, unspecified)
        return view.measuredWidth
    }

    fun setOnTabClick(tab: Tab, onClick: () -> Unit) {
        tabs.getValue(tab).tab.setOnClickListener { onClick() }
    }

    fun setOnAddClick(onClick: () -> Unit) {
        view.findViewById<View>(R.id.glass_footer_add).setOnClickListener { onClick() }
    }

    /** Selects [tab], sliding the pill across; [then] runs once the animation has finished. */
    fun select(tab: Tab, then: (() -> Unit)? = null) {
        if (tab == selected) {
            then?.invoke()
            return
        }
        applyTab(tab, animate = true)
        then?.let { view.postDelayed(it, DURATION) }
    }

    private fun applyTab(tab: Tab, animate: Boolean) {
        val outgoing = tabs.getValue(selected)
        val incoming = tabs.getValue(tab)
        selected = tab
        if (animate) {
            // Labels fade while every bound in the footer (tabs, overall width) glides to its new size
            TransitionManager.beginDelayedTransition(
                view as ViewGroup,
                TransitionSet()
                    .addTransition(Fade(Fade.OUT))
                    .addTransition(ChangeBounds())
                    .addTransition(Fade(Fade.IN))
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .setDuration(DURATION)
                    .setInterpolator(FastOutSlowInInterpolator()),
            )
        }
        val density = activity.resources.displayMetrics.density
        tabs.forEach { (t, v) ->
            val active = t == tab
            v.label.isVisible = active
            v.tab.setPadding(((if (active) 14 else 11) * density).toInt(), 0, ((if (active) 16 else 11) * density).toInt(), 0)
            v.tab.contentDescription = v.label.text
            v.tab.isSelected = active
            if (!animate) v.tint(if (active) onPrimary else onSurface)
        }
        val evaluator = ArgbEvaluator()
        row.select(incoming.tab, animate, DURATION) { f ->
            if (!animate) return@select
            incoming.tint(evaluator.evaluate(f, onSurface, onPrimary) as Int)
            outgoing.tint(evaluator.evaluate(f, onPrimary, onSurface) as Int)
        }
    }

    companion object {
        const val DURATION = 280L
        const val TRANSITION_NAME = "glass_footer"

        fun bind(activity: Activity, footer: View, selected: Tab): GlassFooter {
            footer.transitionName = TRANSITION_NAME
            return GlassFooter(activity, footer, selected)
        }
    }
}
