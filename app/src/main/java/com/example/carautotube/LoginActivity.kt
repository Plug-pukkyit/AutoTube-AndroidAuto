package com.example.carautotube

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

@SuppressLint("SetJavaScriptEnabled")
class LoginActivity : Activity() {
    private lateinit var web: WebView

    // ใช้ User-Agent ของ Chrome บน macOS เพื่อเลี่ยงการตรวจจับฟีเจอร์เฉพาะของ Android WebView
    private val customUa = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)

        val cm = CookieManager.getInstance()
        cm.setAcceptCookie(true)
        cm.setAcceptThirdPartyCookies(web, true)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true

            // หลอกเป็น Desktop Chrome
            userAgentString = customUa

            // ปิดฟีเจอร์ที่ระบุตัวตนเป็น WebView
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        web.webChromeClient = WebChromeClient()

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                // ดักจับและอนุญาตตามปกติ
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(v: WebView, url: String, favicon: Bitmap?) {
                // ฉีด Script ลึกระดับโปรโตไทป์ เพื่อลบ webdriver และจำลอง window.chrome ให้เหมือนเบราว์เซอร์จริง
                val injection = """
                    (function() {
                        Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
                        Object.defineProperty(navigator, 'userAgent', { get: () => '$customUa' });
                        Object.defineProperty(navigator, 'appVersion', { get: () => '$customUa' });
                        Object.defineProperty(navigator, 'platform', { get: () => 'MacIntel' });
                        
                        if (!window.chrome) {
                            window.chrome = {
                                runtime: {},
                                loadTimes: function() {},
                                csi: function() {},
                                app: {}
                            };
                        }
                    })();
                """.trimIndent()
                v.evaluateJavascript(injection, null)

                val host = android.net.Uri.parse(url).host ?: return
                // เมื่อล็อกอินเสร็จและกลับมายังหน้าเว็บ YouTube
                if (host.endsWith("youtube.com") && !url.contains("ServiceLogin") && !url.contains("signin")) {
                    cm.flush()
                    val cookies = cm.getCookie(url) ?: ""
                    if (cookies.contains("LOGIN_INFO") || cookies.contains("SID") || cookies.contains("SAPISID")) {
                        Toast.makeText(
                            this@LoginActivity,
                            "เข้าสู่ระบบสำเร็จแล้ว! ปิดหน้านี้แล้วเปิดบนจอรถได้เลย",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        }

        setContentView(web)
        // เข้าหน้าล็อกอินผ่านหน้าแรกของ YouTube Desktop เพื่อไม่ให้โดนดักจับที่โดเมน accounts.google.com โดยตรง
        web.loadUrl("https://www.youtube.com")
    }

    override fun onPause() {
        CookieManager.getInstance().flush()
        super.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}