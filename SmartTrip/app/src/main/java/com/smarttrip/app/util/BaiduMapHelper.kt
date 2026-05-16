package com.smarttrip.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.smarttrip.app.model.TransportMode

object BaiduMapHelper {

    fun openNavigation(
        context: Context,
        fromName: String,
        toName: String,
        transportMode: TransportMode
    ) {
        val uri = Uri.parse(
            "baidumap://map/direction?" +
                    "origin=latlng:0,0|name:$fromName" +
                    "&destination=latlng:0,0|name:$toName" +
                    "&mode=${transportMode.toBaiduMapMode()}" +
                    "&src=smarttrip"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse(
                "market://details?id=com.baidu.BaiduMap"
            )).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        }
    }
}
