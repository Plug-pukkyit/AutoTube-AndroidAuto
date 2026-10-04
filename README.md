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
- โค้ดนี้เขียนโดยทดสอบกับรถจริง (Yaris Cross HEV 2023)
  ยังไม่ได้ทดสอบกับรถรุ่นอื่นครับ
- ใช้ผ่านเว็บ YouTube ปกติ ไม่ดึงสตรีม ไม่บล็อกโฆษณา (ตามข้อกำหนดของ YouTube)
- `ALLOW_ALL_HOSTS_VALIDATOR` ใช้สำหรับ sideload เท่านั้น

### ทางเลือกที่แนะนำ: การติดตั้งผ่าน KingInstaller (แก้ปัญหาแอปไม่ขึ้นจอรถ)

ในระบบปฏิบัติการ Android 11 ขึ้นไป (รวมถึง Alldocube iPlay 20S) การติดตั้งไฟล์ `.apk` โดยตรงบางครั้งอาจถูกระบบรักษาความปลอดภัยของ Android Auto ซ่อนไอคอนไม่ให้ขึ้นหน้าจอรถ การติดตั้งผ่าน **KingInstaller** จะช่วยปลอมแปลง Package Installer ให้เป็น `com.android.vending` (Google Play Store) ทำให้แอปเปิดบนจอรถได้แน่นอน 100%

#### 1. ดาวน์โหลดเครื่องมือที่จำเป็น
1. ดาวน์โหลดไฟล์ `KingInstaller.apk` จาก GitHub ทางการ: [KingInstaller Releases](https://github.com/fcaronte/KingInstaller/releases)
2. ดาวน์โหลดไฟล์ `app-release.apk` ของ **AutoTube** จากเมนู [Releases](../../releases) ของโปรเจกต์นี้มาเก็บไว้ในความจุเครื่องแท็บเล็ต/มือถือ

#### 2. ตั้งค่าเตรียมพร้อมก่อนติดตั้ง
1. ติดตั้งและเปิดแอป **KingInstaller** บนแท็บเล็ต/มือถือ
2. หากระบบถามสิทธิ์ **"Install unknown apps" (ติดตั้งแอปที่ไม่รู้จัก)** ให้กดยินยอม (Allow) ให้กับ KingInstaller
3. เปิดแอป **Android Auto** บนแท็บเล็ต/มือถือ:
   * ไปที่ **Settings** > แตะที่ **Version** รัวๆ 10 ครั้งเพื่อเปิด Developer Settings
   * แตะจุด 3 จุดมุมขวาบน > เลือก **Developer settings**
   * ติ๊กถูกที่ **Unknown sources** และเปลี่ยน **Application Mode** เป็น **Developer**

#### 3. ขั้นตอนการติดตั้ง AutoTube ผ่าน KingInstaller
1. เปิดแอป **KingInstaller**
2. กดปุ่ม **"Select file"** แล้วเลือกไฟล์ `app-release.apk` ของ AutoTube ที่ดาวน์โหลดเก็บไว้
3. การตั้งค่าใน KingInstaller (แนะนำ):
   * ติ๊กถูกที่ช่อง **"Enable if you use Android 11"** (จำเป็นสำหรับ Android 11)
   * ติ๊กถูกที่ช่อง **"Enable if you use KingInstaller method"**
4. กดปุ่ม **"Install as King"**
5. หน้าต่างระบบจะเด้งขึ้นมาถามยืนยันการติดตั้ง ให้กด **"Install" (ติดตั้ง)**
6. เมื่อติดตั้งเสร็จสิ้น ให้เปิดแอป AutoTube บนเครื่อง 1 ครั้งเพื่อตั้งค่าและอนุญาตสิทธิ์การทำงาน
