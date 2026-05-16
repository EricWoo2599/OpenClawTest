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

            // 方式1: 百度地图导航URI (推荐)
            val navUriStr = "baidumap://map/navi?" +
                    "destination=name:$encodedToName" +
                    "&coord_type=bd09ll" +
                    "&src=andr.com.smarttrip.app"
            
            val navUri = Uri.parse(navUriStr)
            val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(navIntent)
                Toast.makeText(context, "正在打开百度地图...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // 方式2: 百度地图路线规划
                val directionUriStr = "baidumap://map/direction?" +
                        "destination=name:$encodedToName" +
                        "&mode=$mode" +
                        "&coord_type=bd09ll" +
                        "&src=andr.com.smarttrip.app"
                val directionUri = Uri.parse(directionUriStr)
                val directionIntent = Intent(Intent.ACTION_VIEW, directionUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(directionIntent)
                    Toast.makeText(context, "正在打开百度地图...", Toast.LENGTH_SHORT).show()
                } catch (e2: Exception) {
                    // 方式3: 带起点和终点的路线规划
                    val fullDirectionUriStr = "baidumap://map/direction?" +
                            "origin=name:$encodedFromName" +
                            "&destination=name:$encodedToName" +
                            "&mode=$mode" +
                            "&coord_type=bd09ll" +
                            "&src=andr.com.smarttrip.app"
                    val fullDirectionUri = Uri.parse(fullDirectionUriStr)
                    val fullDirectionIntent = Intent(Intent.ACTION_VIEW, fullDirectionUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(fullDirectionIntent)
                        Toast.makeText(context, "正在打开百度地图...", Toast.LENGTH_SHORT).show()
                    } catch (e3: Exception) {
                        // 方式4: 尝试打开百度地图应用
                        val appUri = Uri.parse("baidumap://map/")
                        val appIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(appIntent)
                            Toast.makeText(context, "请手动搜索: $toName", Toast.LENGTH_LONG).show()
                        } catch (e4: Exception) {
                            // 最后的回退：打开网页版百度地图
                            Toast.makeText(context, "未检测到百度地图，正在打开网页版...", Toast.LENGTH_LONG).show()
                            val webUri = Uri.parse("https://map.baidu.com/dir/?src=andr.com.smarttrip.app")
                            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(webIntent)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "打开地图失败，请手动打开百度地图", Toast.LENGTH_LONG).show()
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
