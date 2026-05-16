package com.smarttrip.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.smarttrip.app.model.TransportMode
import java.net.URLEncoder

object BaiduMapHelper {

    fun openNavigation(
        context: Context,
        fromName: String,
        toName: String,
        transportMode: TransportMode
    ) {
        try {
            val encodedFromName = URLEncoder.encode(fromName, "UTF-8")
            val encodedToName = URLEncoder.encode(toName, "UTF-8")
            val mode = transportMode.toBaiduMapMode()

            // 尝试多种调用方式，确保兼容性
            val uriStr = "baidumap://map/direction?" +
                    "origin=name:$encodedFromName" +
                    "&destination=name:$encodedToName" +
                    "&mode=$mode" +
                    "&coord_type=bd09ll" +
                    "&src=andr.com.smarttrip.app"
            
            val uri = Uri.parse(uriStr)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // 如果失败，尝试简化版本
                val simplifiedUriStr = "baidumap://map/direction?" +
                        "destination=name:$encodedToName" +
                        "&mode=$mode" +
                        "&coord_type=bd09ll" +
                        "&src=andr.com.smarttrip.app"
                val simplifiedUri = Uri.parse(simplifiedUriStr)
                val simplifiedIntent = Intent(Intent.ACTION_VIEW, simplifiedUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(simplifiedIntent)
                } catch (e2: Exception) {
                    // 再尝试直接导航到终点
                    val navUriStr = "baidumap://map/navi?" +
                            "query=$encodedToName" +
                            "&coord_type=bd09ll" +
                            "&src=andr.com.smarttrip.app"
                    val navUri = Uri.parse(navUriStr)
                    val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(navIntent)
                    } catch (e3: Exception) {
                        // 最后的回退：打开网页版百度地图
                        Toast.makeText(context, "正在打开网页版地图...", Toast.LENGTH_SHORT).show()
                        val webUri = Uri.parse("https://map.baidu.com/search/$encodedToName/")
                        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "打开地图失败，请尝试手动打开百度地图", Toast.LENGTH_LONG).show()
        }
    }

    fun isBaiduMapInstalled(context: Context): Boolean {
        return try {
            val uri = Uri.parse("baidumap://map/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            val activities = context.packageManager.queryIntentActivities(intent, 0)
            activities.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
