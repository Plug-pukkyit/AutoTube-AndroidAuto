package com.example.carautotube

import android.annotation.SuppressLint
import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.SystemClock
import android.view.Display
import android.view.Gravity
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView

/**
 * หน้าต่าง Presentation บนจอรถ:
 * เลเยอร์หลัก: WebView สำหรับ YouTube
 * เลเยอร์เสริม: Floating Mini-Map (Google Maps) พร้อมปุ่มเปิด/ปิด
 */
@SuppressLint("SetJavaScriptEnabled")
class WebPresentation(
    outer: Context,
    display: Display,
    private val startUrl: String,
    private val desktop: Boolean,
) : Presentation(outer, display) {

    lateinit var webView: WebView
        private set
    private lateinit var mapWebView: WebView
    private lateinit var mapContainer: FrameLayout
    private lateinit var toggleMapButton: Button
    private lateinit var root: FrameLayout
    private lateinit var banner: TextView
    private var customView: View? = null
    private var customCallback: WebChromeClient.CustomViewCallback? = null
    private var blocked = false
    private var isMapVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = FrameLayout(context).apply { setBackgroundColor(Color.BLACK) }

        // 1. YouTube WebView หลัก
        webView = WebView(context).apply {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                loadWithOverviewMode = true
                useWideViewPort = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                userAgentString = if (desktop) Prefs.DESKTOP_UA else userAgentString.replace("; wv", "")
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest) = false
            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View, cb: CustomViewCallback) {
                    customView?.let { root.removeView(it) }
                    customView = view; customCallback = cb
                    root.addView(view, FrameLayout.LayoutParams(MATCH, MATCH))
                    banner.bringToFront()
                    mapContainer.bringToFront()
                    toggleMapButton.bringToFront()
                }
                override fun onHideCustomView() {
                    customView?.let { root.removeView(it) }
                    customView = null
                    customCallback?.onCustomViewHidden()
                    customCallback = null
                }
            }
        }

        // 2. Google Maps WebView ย่อส่วน (Floating Mini Map)
        val density = context.resources.displayMetrics.density
        val mapWidth = (320 * density).toInt()   // กว้างประมาณ 320dp
        val mapHeight = (220 * density).toInt()  // สูงประมาณ 220dp

        mapWebView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                setGeolocationEnabled(true)
                loadWithOverviewMode = true
                useWideViewPort = true
                userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest) = false
            }
            loadUrl("https://www.google.com/maps")
        }

        // กรอบของ Mini Map (ใส่ขอบมนและเส้นขอบสีขาวโปร่งแสง)
        mapContainer = FrameLayout(context).apply {
            val bg = GradientDrawable().apply {
                cornerRadius = 16 * density
                setStroke((2 * density).toInt(), Color.parseColor("#88FFFFFF"))
                setColor(Color.BLACK)
            }
            background = bg
            clipToOutline = true
            visibility = View.GONE
            addView(mapWebView, FrameLayout.LayoutParams(MATCH, MATCH))
        }

        val mapParams = FrameLayout.LayoutParams(mapWidth, mapHeight).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            leftMargin = (16 * density).toInt()
            bottomMargin = (16 * density).toInt()
        }

        // 3. ปุ่มสลับเปิด/ปิด Mini Map
        toggleMapButton = Button(context).apply {
            text = "🗺️ Map"
            textSize = 14f
            setTextColor(Color.WHITE)
            val btnBg = GradientDrawable().apply {
                cornerRadius = 20 * density
                setColor(Color.parseColor("#CC222222"))
            }
            background = btnBg
            setOnClickListener { toggleMap() }
        }

        val btnParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            (40 * density).toInt()
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            leftMargin = (16 * density).toInt()
            topMargin = (16 * density).toInt()
        }

        // 4. แบนเนอร์เตือนความปลอดภัย
        banner = TextView(context).apply {
            text = "กำลังขับรถ — จอดรถให้สนิทก่อนดูวิดีโอ"
            textSize = 36f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
            gravity = Gravity.CENTER
            visibility = View.GONE
        }

        // ประกอบ View เข้ากับ Root Container
        root.addView(webView, FrameLayout.LayoutParams(MATCH, MATCH))
        root.addView(mapContainer, mapParams)
        root.addView(toggleMapButton, btnParams)
        root.addView(banner, FrameLayout.LayoutParams(MATCH, MATCH))

        setContentView(root)
        webView.loadUrl(startUrl)
    }

    private fun toggleMap() {
        isMapVisible = !isMapVisible
        mapContainer.visibility = if (isMapVisible) View.VISIBLE else View.GONE
        toggleMapButton.text = if (isMapVisible) "❌ ปิด Map" else "🗺️ Map"
        if (isMapVisible) {
            mapContainer.bringToFront()
            toggleMapButton.bringToFront()
        }
    }

    fun currentUrl(): String? = webView.url

    fun setBlocked(value: Boolean) {
        if (value == blocked) return
        blocked = value
        banner.visibility = if (value) View.VISIBLE else View.GONE
        if (value) {
            webView.evaluateJavascript(
                "document.querySelectorAll('video,audio').forEach(function(v){v.pause();})", null
            )
        }
    }

    /** จัดการส่งการแตะจอ (Touch Event) ไปยัง View ที่ถูกต้อง */
    fun tap(x: Float, y: Float) {
        if (blocked) return

        // ตรวจสอบว่าแตะโดนปุ่ม Toggle Map หรือไม่
        if (isViewHit(toggleMapButton, x, y)) {
            toggleMap()
            return
        }

        // ตรวจสอบว่าแตะในกรอบ Mini Map หรือไม่
        if (isMapVisible && isViewHit(mapContainer, x, y)) {
            val location = IntArray(2)
            mapContainer.getLocationOnScreen(location)
            val relX = x - location[0]
            val relY = y - location[1]
            dispatchTouch(mapWebView, relX, relY)
            return
        }

        // แตะพื้นที่ YouTube ทั่วไป
        dispatchTouch(webView, x, y)
    }

    private fun isViewHit(view: View, x: Float, y: Float): Boolean {
        if (view.visibility != View.VISIBLE) return false
        val loc = IntArray(2)
        view.getLocationOnScreen(loc)
        return x >= loc[0] && x <= (loc[0] + view.width) &&
                y >= loc[1] && y <= (loc[1] + view.height)
    }

    private fun dispatchTouch(target: View, x: Float, y: Float) {
        val down = SystemClock.uptimeMillis()
        val d = MotionEvent.obtain(down, down, MotionEvent.ACTION_DOWN, x, y, 0)
            .apply { source = InputDevice.SOURCE_TOUCHSCREEN }
        val u = MotionEvent.obtain(down, down + 60, MotionEvent.ACTION_UP, x, y, 0)
            .apply { source = InputDevice.SOURCE_TOUCHSCREEN }
        target.dispatchTouchEvent(d)
        target.dispatchTouchEvent(u)
        d.recycle(); u.recycle()
    }

    fun scrollBy(dx: Float, dy: Float) {
        if (blocked) return
        webView.scrollBy(dx.toInt(), dy.toInt())
    }

    fun goBack() {
        if (customView != null) webView.webChromeClient?.onHideCustomView()
        else if (isMapVisible && mapWebView.canGoBack()) mapWebView.goBack()
        else if (webView.canGoBack()) webView.goBack()
    }

    override fun dismiss() {
        runCatching {
            webView.stopLoading()
            mapWebView.stopLoading()
            root.removeAllViews()
            webView.destroy()
            mapWebView.destroy()
        }
        super.dismiss()
    }

    companion object {
        private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    }
}