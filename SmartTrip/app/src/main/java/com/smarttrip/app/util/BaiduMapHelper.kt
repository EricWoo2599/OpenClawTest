package com.smarttrip.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.smarttrip.app.model.TransportMode

object BaiduMapHelper {

    fun openNavigation(
        context: Context,
        fromName: String,
        toName: String,
        transportMode: TransportMode
    ) {
        try {
            val mode = transportMode.toBaiduMapMode()

            // 方式1: 使用正确的百度地图URI格式 - 只有终点
            val intent1 = Intent().apply {
                data = Uri.parse("baidumap://map/direction?destination=name:$toName&mode=$mode&src=andr.smarttrip")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(intent1)
                Toast.makeText(context, "正在打开百度地图...", Toast.LENGTH_SHORT).show()
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 方式2: 使用包名直接启动百度地图
            try {
                val intent2 = Intent(Intent.ACTION_VIEW, Uri.parse("baidumap://map/direction?destination=name:$toName&mode=$mode"))
                intent2.setPackage("com.baidu.BaiduMap")
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent2)
                Toast.makeText(context, "正在打开百度地图...", Toast.LENGTH_SHORT).show()
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 方式3: 使用最简单的导航URI
            try {
                val intent3 = Intent(Intent.ACTION_VIEW, Uri.parse("baidumap://map/navi?query=$toName"))
                intent3.setPackage("com.baidu.BaiduMap")
                intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent3)
                Toast.makeText(context, "正在打开百度地图...", Toast.LENGTH_SHORT).show()
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 方式4: 尝试打开百度地图应用主界面
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.baidu.BaiduMap")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    Toast.makeText(context, "请在百度地图中搜索: $toName", Toast.LENGTH_LONG).show()
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 方式5: 打开网页版百度地图
            Toast.makeText(context, "未检测到百度地图，正在打开网页版...", Toast.LENGTH_LONG).show()
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://map.baidu.com/search/$toName/"))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "请手动打开百度地图搜索: $toName", Toast.LENGTH_LONG).show()
        }
    }

    fun isBaiduMapInstalled(context: Context): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo("com.baidu.BaiduMap", 0)
            packageInfo != null
        } catch (e: Exception) {
            false
        }
    }
}
