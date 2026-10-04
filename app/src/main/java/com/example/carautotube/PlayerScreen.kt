package com.example.carautotube

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.CarValue
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * หน้าจอเดียวของแอปบนรถ: เป็น NavigationTemplate ที่ Android Auto ให้ Surface มาวาดภาพ
 * เราสร้าง VirtualDisplay บน Surface นั้น แล้วแสดง WebPresentation (WebView) บนจอเสมือน
 */
class PlayerScreen(carContext: CarContext) : Screen(carContext) {

    private val prefs = Prefs(carContext)
    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: WebPresentation? = null
    private var lastUrl: String? = null
    private var moving = false

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(container: SurfaceContainer) {
            val surface = container.surface ?: return
            releaseDisplay()
            val dm = carContext.getSystemService(DisplayManager::class.java)
            val vd = dm.createVirtualDisplay(
                "AutoTubeDisplay",
                container.width, container.height, container.dpi,
                surface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
            ) ?: return
            virtualDisplay = vd
            presentation = WebPresentation(
                carContext, vd.display,
                prefs.takePending() ?: lastUrl ?: prefs.startUrl, prefs.desktopMode
            ).also {
                it.show()
                CarBridge.presentation = it
                it.setBlocked(moving)
            }
        }

        override fun onSurfaceDestroyed(container: SurfaceContainer) = releaseDisplay()

        override fun onClick(x: Float, y: Float) { presentation?.tap(x, y) }

        override fun onScroll(distanceX: Float, distanceY: Float) {
            presentation?.scrollBy(distanceX, distanceY)
        }
    }

    init {
        val app = carContext.getCarService(AppManager::class.java)
        app.setSurfaceCallback(surfaceCallback)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                app.setSurfaceCallback(null)
                releaseDisplay()
            }
        })
        listenSpeed()
    }

    private fun releaseDisplay() {
        presentation?.let { lastUrl = it.currentUrl() ?: lastUrl }
        if (CarBridge.presentation === presentation) CarBridge.presentation = null
        runCatching { presentation?.dismiss() }
        presentation = null
        virtualDisplay?.release()
        virtualDisplay = null
    }

    /**
     * ล็อกภาพเมื่อรถเคลื่อนที่ — ทำได้เฉพาะเมื่อรถ/Android Auto ส่งความเร็วมา
     * ถ้าไม่ส่ง (status ไม่สำเร็จ) จะไม่บล็อก
     */
    private fun listenSpeed() {
        runCatching {
            val hw = carContext.getCarService(CarContext.HARDWARE_SERVICE) as CarHardwareManager
            hw.carInfo.addSpeedListener(ContextCompat.getMainExecutor(carContext)) { speed ->
                val v = speed.displaySpeedMetersPerSecond
                val value = v.value
                if (v.status == CarValue.STATUS_SUCCESS && value != null) {
                    moving = prefs.lockWhileDriving && value > 1.5f // ~5.4 กม./ชม.
                    presentation?.setBlocked(moving)
                }
            }
        }
    }

    private fun iconAction(res: Int, onClick: () -> Unit): Action =
        Action.Builder()
            .setIcon(
                CarIcon.Builder(IconCompat.createWithResource(carContext, res))
                    .setTint(CarColor.DEFAULT).build()
            )
            .setOnClickListener { onClick() }
            .build()

    override fun onGetTemplate(): Template {
        val actions = ActionStrip.Builder()
            .addAction(iconAction(R.drawable.ic_back) { presentation?.goBack() })
            .addAction(iconAction(R.drawable.ic_home) { presentation?.webView?.loadUrl(prefs.startUrl) })
            .addAction(iconAction(R.drawable.ic_refresh) { presentation?.webView?.reload() })
            .build()
        return NavigationTemplate.Builder()
            .setActionStrip(actions)
            // Action.PAN เปิดโหมดลากนิ้วเลื่อนหน้าจอ (ส่ง onScroll มาให้)
            .setMapActionStrip(ActionStrip.Builder().addAction(Action.PAN).build())
            .build()
    }
}
