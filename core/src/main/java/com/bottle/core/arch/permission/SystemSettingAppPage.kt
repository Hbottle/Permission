package com.bottle.core.arch.permission

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * 打开系统设置中app的设置页面
 */

fun getAlertWindowSettingIntent(context: Context): Intent {
    return when {
        MARK.contains("huawei") -> huaweiApi(
            context
        )
        MARK.contains("xiaomi") -> xiaomiApi(
            context
        )
        MARK.contains("oppo") -> oppoApi(
            context
        )
        MARK.contains("vivo") -> vivoApi(
            context
        )
        MARK.contains("meizu") -> meizuApi(
            context
        )
        else -> defaultApi(context)
    }
}

private fun defaultApi(context: Context): Intent {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", context.packageName, null)
    return intent
}

private fun huaweiApi(context: Context): Intent {
    val intent = Intent()
    intent.component =
        ComponentName(
            "com.huawei.systemmanager",
            "com.huawei.permissionmanager.ui.MainActivity"
        )
    if (hasActivity(context, intent)) return intent
    intent.component = ComponentName(
        "com.huawei.systemmanager",
        "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity"
    )
    if (hasActivity(context, intent)) return intent
    intent.component = ComponentName(
        "com.huawei.systemmanager",
        "com.huawei.notificationmanager.ui.NotificationManagmentActivity"
    )
    return intent
}

private fun xiaomiApi(context: Context): Intent {
    val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
    intent.putExtra("extra_pkgname", context.packageName)
    if (hasActivity(context, intent)) return intent

    intent.setPackage("com.miui.securitycenter")
    if (hasActivity(context, intent)) return intent

    intent.setClassName(
        "com.miui.securitycenter",
        "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
    )
    return intent
}

private fun oppoApi(context: Context): Intent {
    val intent = Intent()
    intent.putExtra("packageName", context.packageName)
    intent.setClassName(
        "com.color.safecenter",
        "com.color.safecenter.permission.floatwindow.FloatWindowListActivity"
    )
    if (hasActivity(context, intent)) return intent

    intent.setClassName(
        "com.coloros.safecenter",
        "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity"
    )
    if (hasActivity(context, intent)) return intent

    intent.setClassName(
        "com.oppo.safe",
        "com.oppo.safe.permission.PermissionAppListActivity"
    )
    return intent
}

private fun vivoApi(context: Context): Intent {
    val intent = Intent()
    intent.setClassName(
        "com.iqoo.secure",
        "com.iqoo.secure.ui.phoneoptimize.FloatWindowManager"
    )
    intent.putExtra("packagename", context.packageName)
    if (hasActivity(context, intent)) return intent

    intent.component =
        ComponentName(
            "com.iqoo.secure",
            "com.iqoo.secure.safeguard.SoftPermissionDetailActivity"
        )
    return intent
}

private fun meizuApi(context: Context): Intent {
    val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
    intent.putExtra("packageName", context.packageName)
    intent.component =
        ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity")
    return intent
}