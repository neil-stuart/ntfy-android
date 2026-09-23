package io.heckel.ntfy.ui.theme

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.google.android.material.color.MaterialColors
import io.heckel.ntfy.R

/**
 * Window backdrop: three soft colour fields drifting slowly over the theme's ground, giving the
 * translucent glass something to refract (after Nocean's Aurora). The drift is slow enough that
 * ~15 fps looks identical to 60 fps, and it only ticks while the window is visible.
 */
class AuroraDrawable(context: Context) : Drawable() {
    private val base = MaterialColors.getColor(context, R.attr.glassBase, 0)
    private val blobs = intArrayOf(
        MaterialColors.getColor(context, R.attr.glassBlob1, 0),
        MaterialColors.getColor(context, R.attr.glassBlob2, 0),
        MaterialColors.getColor(context, R.attr.glassBlob3, 0),
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handler = Handler(Looper.getMainLooper())
    private var ticking = false
    private val tick = object : Runnable {
        override fun run() {
            invalidateSelf()
            if (ticking) handler.postDelayed(this, FRAME_MS)
        }
    }

    override fun draw(canvas: Canvas) {
        if (isVisible && !ticking) startTicking()
        canvas.drawColor(base)
        val t = ((SystemClock.uptimeMillis() - EPOCH) % CYCLE_MS) / (CYCLE_MS / 2f)
        val p = if (t <= 1f) t else 2f - t // ping-pong 0..1..0
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        if (w <= 0f || h <= 0f) return // Not laid out yet (e.g. drawn early by the footer's blur)
        blob(canvas, w * (0.10f + 0.25f * p), h * (0.10f + 0.10f * p), w * 0.95f, blobs[0])
        blob(canvas, w * (0.95f - 0.30f * p), h * (0.50f - 0.12f * p), w * 0.85f, blobs[1])
        blob(canvas, w * (0.25f + 0.35f * p), h * (0.95f - 0.08f * p), w * 0.90f, blobs[2])
    }

    private fun blob(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        paint.shader = RadialGradient(x, y, radius, color, color and 0x00FFFFFF, Shader.TileMode.CLAMP)
        canvas.drawCircle(x, y, radius, paint)
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        val changed = super.setVisible(visible, restart)
        if (visible) startTicking() else stopTicking()
        return changed
    }

    private fun startTicking() {
        if (ticking) return
        ticking = true
        handler.postDelayed(tick, FRAME_MS)
    }

    private fun stopTicking() {
        ticking = false
        handler.removeCallbacks(tick)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.OPAQUE

    companion object {
        // One shared clock, so every screen's aurora is in the same phase and page transitions
        // crossfade between identical backdrops instead of visibly jumping
        private val EPOCH = SystemClock.uptimeMillis()
        private const val FRAME_MS = 66L
        private const val CYCLE_MS = 44_000L
    }
}
