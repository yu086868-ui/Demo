package com.example.demo

import android.app.Application
import com.amap.api.maps.MapsInitializer

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 高德地图隐私合规配置
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        // 定位SDK隐私合规配置
        com.amap.api.location.AMapLocationClient.updatePrivacyShow(this, true, true)
        com.amap.api.location.AMapLocationClient.updatePrivacyAgree(this, true)

        android.util.Log.d("MyApplication", "高德地图隐私合规配置完成")
    }
}