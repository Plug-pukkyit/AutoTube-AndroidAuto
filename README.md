# AutoTube — เล่นเว็บ/YouTube บนจอ Android Auto (sideload)

## หลักการ
Android Auto ไม่มี template วิดีโอ แต่แอปประเภท **Navigation** ได้ Surface (`SurfaceCallback`)
สำหรับวาดแผนที่ แอปนี้เอา Surface นั้นไปผูกกับ `VirtualDisplay` แล้วแสดง `Presentation`
ที่มี WebView เล่น YouTube บนจอเสมือน การแตะ/ลากบนจอรถถูกแปลงเป็น touch ของ WebView
เสียงออกผ่านโทรศัพท์ → Android Auto → ลำโพงรถ ตามปกติ

ต่างจาก APK เดิม (`com.example.carautotube`) ที่ประกาศเป็น MediaBrowserService
จึงขึ้น "No items" และเปิด Activity บนจอแท็บเล็ต

## Build
1. ติดตั้ง Android Studio (Koala/ใหม่กว่า) แล้ว Open โฟลเดอร์ `AutoTube`
2. รอ Gradle sync (ถ้าถามให้สร้าง wrapper ให้ตอบ OK)
3. Build > Build APK(s) แล้วลงบนโทรศัพท์/แท็บเล็ต (`app/build/outputs/apk/`)
   หรือกด Run ถ้าเชื่อม adb อยู่
   (release ใช้ debug key เซ็นอัตโนมัติ ลงเครื่องได้ทันที)

## ตั้งค่า Android Auto (ครั้งเดียว)
1. ตั้งค่า > Android Auto > กดที่ "เวอร์ชัน" รัวๆ 10 ครั้ง > ยืนยัน Developer mode
2. เมนู ⋮ > Developer settings > เปิด **Unknown sources**
3. ปิด-เปิด Android Auto ใหม่ / เสียบรถใหม่
4. บนจอรถ เปิดตัวเลือกแอป จะเห็น **AutoTube** (อยู่ในกลุ่มนำทาง)

## ถ้ายังไม่เห็นแอป / มีปัญหา
- ลบแอป `com.example.carautotube` เดิมออกก่อน กันสับสน
- Android Auto > Developer settings > ดู "Application mode" และลองเปิด/ปิด "Unknown sources"
- ดู log: `adb logcat | grep -i -E "carapp|autotube"`
- ถ้าแตะจอรถไม่ตอบ: ในแถบปุ่มมุมจอ ลองกดไอคอนมือ (PAN) แล้วแตะ/ลากอีกครั้ง
- ถ้า YouTube ขึ้นหน้าเข้าสู่ระบบ/ตัวอย่างเพลงไม่เล่น ลองสลับโหมดเดสก์ท็อปในหน้าตั้งค่า

## ข้อควรระวัง
- โค้ดนี้เขียนโดยไม่ได้คอมไพล์/ทดสอบกับรถจริง (Yaris Cross HEV 2023)
  อาจต้องแก้เล็กน้อยตาม Android Auto เวอร์ชันที่ติดตั้ง
- ระบบล็อกตอนขับขึ้นกับว่ารถส่งความเร็วมาให้ Android Auto หรือไม่
  ถ้าไม่ส่ง ล็อกจะไม่ทำงาน — ใช้เมื่อจอดสนิทเท่านั้น
- ใช้ผ่านเว็บ YouTube ปกติ ไม่ดึงสตรีม ไม่บล็อกโฆษณา (ตามข้อกำหนดของ YouTube)
- `ALLOW_ALL_HOSTS_VALIDATOR` ใช้สำหรับ sideload เท่านั้น
