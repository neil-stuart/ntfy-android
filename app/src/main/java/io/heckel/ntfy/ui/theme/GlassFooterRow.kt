package io.heckel.ntfy.ui.theme

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.color.MaterialColors
import io.heckel.ntfy.R

/**
 * The footer's tab row. It draws the active-tab pill itself, so when the selection changes the
 * pill slides and reshapes from the old tab to the new one (tracking the tab's own animated
 * bounds) instead of jumping between two backgrounds.
 */
class GlassFooterRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(context, R.attr.colorPrimary, 0)
    }
    private val from = RectF()
    private val pill = RectF()
    private var fraction = 1f
    private var animator: ValueAnimator? = null

    var activeTab: View? = null
        private set

    /** Moves the pill to [tab]; [onFrame] receives the 0..1 progress so callers can blend colours. */
    fun select(tab: View, animate: Boolean, duration: Long, onFrame: (Float) -> Unit) {
        animator?.cancel()
        val previous = activeTab
        activeTab = tab
        if (!animate || previous == null || !isLaidOut) {
            fraction = 1f
            onFrame(1f)
            invalidate()
            return
        }
        from.set(pill)
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                fraction = it.animatedValue as Float
                onFrame(fraction)
                invalidate()
            }
            start()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        activeTab?.let { tab ->
            // The tab's bounds are themselves animating (ChangeBounds), so lerp towards where it is now
            pill.set(
                lerp(from.left, tab.left.toFloat()),
                lerp(from.top, tab.top.toFloat()),
                lerp(from.right, tab.right.toFloat()),
                lerp(from.bottom, tab.bottom.toFloat()),
            )
            val radius = pill.height() / 2
            canvas.drawRoundRect(pill, radius, radius, paint)
        }
        super.dispatchDraw(canvas)
    }

    private fun lerp(start: Float, end: Float) = start + (end - start) * fraction
}
