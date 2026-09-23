package io.heckel.ntfy.ui.theme

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import com.google.android.material.color.MaterialColors
import io.heckel.ntfy.R

/**
 * A frosted pane: translucent fill with a hairline edge that fades from bright at the top to
 * faint at the bottom, like light catching the rim of glass (after Nocean's Modifier.glass).
 */
class GlassDrawable(
    context: Context,
    private val radius: Float,
    fillColor: Int = MaterialColors.getColor(context, R.attr.glassPane, 0),
) : Drawable() {
    private val borderTop = MaterialColors.getColor(context, R.attr.glassBorderTop, 0)
    private val borderBottom = MaterialColors.getColor(context, R.attr.glassBorderBottom, 0)
    private val strokeWidth = context.resources.getDimension(R.dimen.glass_stroke_width)
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillColor }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@GlassDrawable.strokeWidth
    }
    private val rect = RectF()

    var fillColor: Int
        get() = fill.color
        set(value) {
            fill.color = value
            invalidateSelf()
        }

    override fun onBoundsChange(bounds: android.graphics.Rect) {
        val half = strokeWidth / 2
        rect.set(bounds.left + half, bounds.top + half, bounds.right - half, bounds.bottom - half)
        stroke.shader = LinearGradient(0f, rect.top, 0f, rect.bottom, borderTop, borderBottom, Shader.TileMode.CLAMP)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(rect, radius, radius, fill)
        canvas.drawRoundRect(rect, radius, radius, stroke)
    }

    override fun getOutline(outline: Outline) {
        outline.setRoundRect(bounds, radius)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
