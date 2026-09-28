package com.mkrinfinity.autooptimizer

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var dashboard: DashboardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(36,36,36)
        window.navigationBarColor = Color.rgb(36,36,36)
        dashboard = DashboardView()
        setContentView(dashboard)
    }

    private inner class DashboardView : View(this@MainActivity) {
        private val bg = Color.rgb(36,36,36)
        private val cyan = Color.rgb(8,184,200)
        private val text = Color.rgb(242,242,242)
        private val muted = Color.rgb(184,184,184)
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var snapshot = SystemStats.snapshot(this@MainActivity)
        private var page = "home"
        private val features = listOf(
            "File Manager","Process","Connection Control","Deep Clean","Auto restart","Auto terminate",
            "Device info","Memory info","Battery info","CPU info","App info","Auto-rotate Control",
            "Rotation Control","Video Enhancer","Touch Block","Split Screen","Automate YouTube",
            "Screenshot","Clipboard"
        )

        override fun onDraw(c: Canvas) {
            super.onDraw(c)
            c.drawColor(bg)
            if (page == "home") drawHome(c) else drawInfo(c)
        }

        private fun txt(c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int = text, align: Paint.Align = Paint.Align.LEFT) {
            paint.typeface = Typeface.create("sans", Typeface.NORMAL)
            paint.textSize = size
            paint.color = color
            paint.textAlign = align
            c.drawText(s, x, y, paint)
        }

        private fun rect(c: Canvas, l: Float, t: Float, r: Float, b: Float, color: Int, rad: Float = 8f) {
            paint.color = color
            c.drawRoundRect(l, t, r, b, rad, rad, paint)
        }

        private fun drawHome(c: Canvas) {
            val w = width.toFloat()
            txt(c, "☰", 22f, 42f, 30f)
            paint.typeface = Typeface.DEFAULT_BOLD
            txt(c, "Auto Optimizer", 82f, 41f, 22f)
            txt(c, "●", w - 35f, 42f, 22f, cyan, Paint.Align.CENTER)

            val cy = 120f
            rect(c, 22f, cy, w / 2f - 5, cy + 38, cyan)
            txt(c, "Used", 48f, cy + 26, 17f)
            rect(c, w / 2f + 5, cy, w - 22f, cy + 38, Color.rgb(70,70,70))
            txt(c, "Free", w / 2f + 30, cy + 26, 17f, muted)

            val cx = w / 2f
            val centerY = 300f
            val rad = 112f
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 17f
            paint.strokeCap = Paint.Cap.BUTT
            paint.color = Color.rgb(180,180,180)
            c.drawArc(cx-rad, centerY-rad, cx+rad, centerY+rad, -42f, 264f, false, paint)
            paint.color = cyan
            c.drawArc(cx-rad, centerY-rad, cx+rad, centerY+rad, -42f, 264f * snapshot.ramPct / 100f, false, paint)
            paint.style = Paint.Style.FILL
            rect(c, cx-42, centerY-62, cx+42, centerY-32, Color.rgb(110,110,110), 5f)
            txt(c, "MEMORY", cx, centerY-41, 13f, Color.WHITE, Paint.Align.CENTER)
            txt(c, "${snapshot.ramPct}%", cx, centerY+10, 42f)
            txt(c, "${SystemStats.formatBytes(snapshot.ramUsed)} / ${SystemStats.formatBytes(snapshot.ramTotal)}", cx, centerY+42, 14f, Color.WHITE, Paint.Align.CENTER)

            rect(c, 24f, 465f, 108f, 496f, Color.rgb(110,110,110), 4f)
            txt(c, "STORAGE", 66f, 486f, 13f, Color.WHITE, Paint.Align.CENTER)
            txt(c, "${snapshot.storagePct}%", 66f, 545f, 42f, Color.WHITE, Paint.Align.CENTER)
            paint.color = Color.rgb(190,190,190)
            c.drawRect(156f,470f,w-32f,487f,paint)
            paint.color = cyan
            c.drawRect(156f,470f,156f+(w-188f)*snapshot.storagePct/100f,487f,paint)
            txt(c, "${SystemStats.formatBytes(snapshot.storageUsed)} / ${SystemStats.formatBytes(snapshot.storageTotal)}", w-32f, 510f, 14f, Color.WHITE, Paint.Align.RIGHT)

            val top = 575f
            val cellW = (w-60f)/3f
            for (i in features.indices) {
                val col = i % 3
                val row = i / 3
                val x = 20f + col * cellW
                val y = top + row * 122f
                rect(c, x+25, y, x+87, y+62, iconColor(i), 7f)
                txt(c, iconGlyph(i), x+56, y+42, 27f, Color.WHITE, Paint.Align.CENTER)
                txt(c, features[i], x+56, y+84, 12f, Color.LTGRAY, Paint.Align.CENTER)
            }

            txt(c, "Process", 48f, height-24f, 12f, Color.LTGRAY, Paint.Align.CENTER)
            txt(c, "Cache", 145f, height-24f, 12f, Color.LTGRAY, Paint.Align.CENTER)
            txt(c, "History", 242f, height-24f, 12f, Color.LTGRAY, Paint.Align.CENTER)
            txt(c, "Reconnect", w-55f, height-24f, 12f, Color.LTGRAY, Paint.Align.CENTER)
        }

        private fun iconGlyph(i: Int) = listOf("□","»","⌁","♟","↻","×","▣","⌁","▥","▤","▦","↻","◉","✦","⊘","▤","▶","▧","▤")[i]

        private fun iconColor(i: Int) = when (i % 7) {
            0 -> 0xFFFFB400.toInt()
            1 -> 0xFF2788E0.toInt()
            2 -> 0xFFC55ACB.toInt()
            3 -> 0xFF11B9B4.toInt()
            4 -> 0xFFD73838.toInt()
            5 -> 0xFF0EACE0.toInt()
            else -> 0xFF3E6E9D.toInt()
        }

        private fun drawInfo(c: Canvas) {
            val w = width.toFloat()
            txt(c, "‹", 22f, 50f, 42f)
            txt(c, "About Auto Optimizer", 72f, 45f, 23f)
            paint.color = cyan
            c.drawCircle(70f, 130f, 42f, paint)
            txt(c, "MK", 70f, 142f, 26f, Color.WHITE, Paint.Align.CENTER)
            txt(c, "mkr_infinity", 130f, 122f, 21f)
            txt(c, "Open-source builder • system optimizer", 130f, 150f, 14f, muted)
            txt(c, "Instagram", 28f, 215f, 16f, muted)
            txt(c, "@mkr_infinity", 28f, 242f, 19f)
            txt(c, "Telegram", 28f, 292f, 16f, muted)
            txt(c, "@mkr_infinity", 28f, 319f, 19f)
            txt(c, "GitHub", 28f, 369f, 16f, muted)
            txt(c, "mkr-infinity", 28f, 396f, 19f)
            txt(c, "Website", 28f, 446f, 16f, muted)
            txt(c, "mkr-infinity.github.io", 28f, 473f, 19f)
            txt(c, "Version 0.1.0", 28f, 540f, 14f, muted)
            txt(c, "Built for low-end Android devices", 28f, 570f, 14f, muted)
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            if (e.action != MotionEvent.ACTION_UP) return true
            val x = e.x
            val y = e.y
            if (page == "home") {
                if (y < 70 && x < 70) {
                    page = "info"
                    invalidate()
                    return true
                }
                if (y > topY() && y < topY() + 4 * 122) {
                    val idx = ((y - topY()) / 122).toInt() * 3 + (x / (width / 3f)).toInt()
                    if (idx == 3) {
                        startActivity(Intent(this@MainActivity, DeepCleanActivity::class.java))
                        return true
                    }
                }
                if (y > height - 70 && x < 90) {
                    Toast.makeText(this@MainActivity, "Process tools coming next", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (y < 80) {
                    page = "home"
                    invalidate()
                    return true
                }
                when {
                    y in 210f..270f -> open("https://www.instagram.com/mkr_infinity")
                    y in 285f..345f -> open("https://t.me/mkr_infinity")
                    y in 360f..420f -> open("https://github.com/mkr-infinity")
                    y in 435f..500f -> open("https://mkr-infinity.github.io")
                }
            }
            return true
        }

        private fun topY() = 575f
        private fun open(url: String) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
