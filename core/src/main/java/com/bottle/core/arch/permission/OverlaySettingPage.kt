package com.bottle.core.arch.permission

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * 打开悬浮框权限页面
 */

fun overlaySettingIntent(context: Context): Intent {
    return when {
        MARK.contains("meizu") -> meiZuApi(
            context
        )
        else -> {
            defaultApi(context)
        }
    }
}

private fun meiZuApi(context: Context): Intent {
    val overlayIntent = Intent("com.meizu.safe.security.SHOW_APPSEC")
    overlayIntent.putExtra("packageName", context.packageName)
    overlayIntent.component = ComponentName(
        "com.meizu.safe",
        "com.meizu.safe.security.AppSecActivity"
    )
    if (!resolveActivity(overlayIntent, context)) {
        return appDetailsApi(context)
    }
    return overlayIntent
}

private fun defaultApi(context: Context): Intent {
    val manageIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    manageIntent.data = Uri.fromParts("package", context.packageName, null)
    if (!resolveActivity(manageIntent, context)) {
        return appDetailsApi(context)
    }
    return manageIntent
}

private fun appDetailsApi(context: Context): Intent {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", context.packageName, null)
    return intent
}