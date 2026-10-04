package com.example.carautotube

import android.content.Context

/** ค่าตั้งค่าที่แชร์ระหว่างหน้าจอโทรศัพท์ (MainActivity) กับจอรถ */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("autotube", Context.MODE_PRIVATE)

    var startUrl: String
        get() = sp.getString("url", DEFAULT_URL) ?: DEFAULT_URL
        set(v) = sp.edit().putString("url", v).apply()

    /** true = ใช้เว็บแบบเดสก์ท็อป (เหมาะกับจอรถกว้างๆ) */
    var desktopMode: Boolean
        get() = sp.getBoolean("desktop", true)
        set(v) = sp.edit().putBoolean("desktop", v).apply()

    /** true = บล็อกภาพเมื่อรถเคลื่อนที่ (ถ้ารถส่งข้อมูลความเร็วมา) */
    var lockWhileDriving: Boolean
        get() = sp.getBoolean("lock", true)
        set(v) = sp.edit().putBoolean("lock", v).apply()

    /** URL ที่ค้นหาไว้ตอนยังไม่ได้ต่อจอรถ จะเปิดให้เมื่อจอรถพร้อม */
    fun savePending(url: String) = sp.edit().putString("pending", url).apply()

    fun takePending(): String? {
        val u = sp.getString("pending", null)
        if (u != null) sp.edit().remove("pending").apply()
        return u
    }

    companion object {
        const val DEFAULT_URL = "https://m.youtube.com"
        const val DESKTOP_UA =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0.0.0 Safari/537.36"
    }
}
