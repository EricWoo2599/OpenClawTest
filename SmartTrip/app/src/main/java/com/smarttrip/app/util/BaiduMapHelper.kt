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

            val uri = Uri.parse(
                "baidumap://map/direction?" +
                        "origin=name:$encodedFromName" +
                        "&destination=name:$encodedToName" +
                        "&mode=${transportMode.toBaiduMapMode()}" +
                        "&coord_type=bd09ll" +
                        "&src=andr.com.smarttrip.app"
            )

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "请先安装百度地图", Toast.LENGTH_SHORT).show()
                openBaiduMapDownloadPage(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "打开地图失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openBaiduMapDownloadPage(context: Context) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse(
                "market://details?id=com.baidu.BaiduMap"
            )).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://map.baidu.com"
                )).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun isBaiduMapInstalled(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("baidumap://map/"))
            intent.resolveActivity(context.packageManager) != null
        } catch (e: Exception) {
            false
        }
    }
}
