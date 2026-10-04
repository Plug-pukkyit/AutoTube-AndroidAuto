package com.example.carautotube

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class YouTubeCarAppService : CarAppService() {
    // อนุญาตทุก host เพื่อให้ใช้กับ Android Auto ที่ sideload ได้ (ไม่ใช่แอป Play Store)
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen = PlayerScreen(carContext)
    }
}
