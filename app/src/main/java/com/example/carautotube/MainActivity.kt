package com.example.carautotube

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.*

/** หน้าตั้งค่าบนโทรศัพท์/แท็บเล็ต (วิดีโอจะเล่นบนจอรถ ไม่ใช่หน้านี้) */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        val pad = (16 * resources.displayMetrics.density).toInt()

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        fun add(v: android.view.View) = col.addView(
            v, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        add(TextView(this).apply {
            textSize = 20f
            text = "AutoTube"
        })
        add(TextView(this).apply {
            text = "วิธีใช้\n" +
                "1) เปิด Android Auto > กดเวอร์ชันรัวๆ 10 ครั้ง > เปิด Developer settings\n" +
                "2) ใน Developer settings เปิด \"Unknown sources\"\n" +
                "3) เชื่อมต่อรถ แล้วเลือกแอป AutoTube จากหมวดนำทาง (Navigation) บนจอรถ\n" +
                "4) แตะจอรถเพื่อเลือกวิดีโอ เสียงจะออกลำโพงรถผ่านโทรศัพท์\n\n" +
                "คำเตือน: ห้ามดูวิดีโอขณะขับรถ ใช้เมื่อจอดสนิทเท่านั้น"
        })

        val query = EditText(this).apply {
            hint = "ค้นหา YouTube หรือวางลิงก์วิดีโอ"
            setSingleLine()
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
        }
        val sendToCar = {
            val q = query.text.toString().trim()
            if (q.isNotEmpty()) {
                val target = if (q.startsWith("http://") || q.startsWith("https://")) q
                else "https://m.youtube.com/results?search_query=" +
                    java.net.URLEncoder.encode(q, "UTF-8")
                if (CarBridge.open(target)) {
                    Toast.makeText(this, "ส่งไปจอรถแล้ว", Toast.LENGTH_SHORT).show()
                } else {
                    prefs.savePending(target)
                    Toast.makeText(
                        this, "ยังไม่ได้เปิดบนจอรถ จะเปิดให้เมื่อเปิดแอปบนจอรถ", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        query.setOnEditorActionListener { _, _, _ -> sendToCar(); true }
        add(TextView(this).apply { text = "\nค้นหา / ส่งวิดีโอไปจอรถ:" })
        add(query)
        add(Button(this).apply {
            text = "ค้นหาและเปิดบนจอรถ"
            setOnClickListener { sendToCar() }
        })

        val url = EditText(this).apply {
            setText(prefs.startUrl)
            hint = "URL เริ่มต้น"
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine()
        }
        add(TextView(this).apply { text = "\nหน้าเริ่มต้น:" })
        add(url)

        val desktop = Switch(this).apply {
            text = "โหมดเดสก์ท็อป (จอกว้าง)"
            isChecked = prefs.desktopMode
        }
        val lock = Switch(this).apply {
            text = "บล็อกภาพเมื่อรถเคลื่อนที่ (ถ้ารถส่งความเร็วมา)"
            isChecked = prefs.lockWhileDriving
        }
        add(desktop); add(lock)

        add(Button(this).apply {
            text = "เข้าสู่ระบบ YouTube / Premium (ทำบนโทรศัพท์)"
            setOnClickListener { startActivity(android.content.Intent(context, LoginActivity::class.java)) }
        })
        add(Button(this).apply {
            text = "บันทึก"
            setOnClickListener {
                var u = url.text.toString().trim()
                if (u.isEmpty()) u = Prefs.DEFAULT_URL
                if (!u.startsWith("http")) u = "https://$u"
                prefs.startUrl = u
                prefs.desktopMode = desktop.isChecked
                prefs.lockWhileDriving = lock.isChecked
                Toast.makeText(context, "บันทึกแล้ว (มีผลครั้งถัดไปที่เปิดบนจอรถ)", Toast.LENGTH_LONG).show()
            }
        })

        setContentView(ScrollView(this).apply { addView(col) })
    }
}
