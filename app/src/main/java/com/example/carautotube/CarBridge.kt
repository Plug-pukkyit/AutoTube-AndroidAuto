package com.example.carautotube

/** สะพานระหว่างหน้าโทรศัพท์ (MainActivity) กับจอรถ (อยู่ใน process เดียวกัน) */
object CarBridge {
    @Volatile
    var presentation: WebPresentation? = null

    /** @return true ถ้าจอรถเปิดอยู่และส่ง URL ไปแล้ว */
    fun open(url: String): Boolean {
        val p = presentation ?: return false
        p.webView.post { p.webView.loadUrl(url) }
        return true
    }
}
