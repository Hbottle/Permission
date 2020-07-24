package com.bottle.core.arch.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.bottle.core.R
import java.lang.StringBuilder
import java.util.Arrays
import kotlin.collections.ArrayList

/**
 * 运行时权限，仿照<a href="https://github.com/yanzhenjie/AndroidPermission">AndroidPermission</a>
 * 的kotlin版本
 */
val CALENDAR = arrayOf(
    Manifest.permission.READ_CALENDAR,
    Manifest.permission.WRITE_CALENDAR
)

val CAMERA = arrayOf(Manifest.permission.CAMERA)

val CONTACTS = arrayOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.WRITE_CONTACTS,
    Manifest.permission.GET_ACCOUNTS
)

val LOCATION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

val MICROPHONE = arrayOf(Manifest.permission.RECORD_AUDIO)

val PHONE = arrayOf(
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.CALL_PHONE,
    Manifest.permission.READ_CALL_LOG,
    Manifest.permission.WRITE_CALL_LOG,
    Manifest.permission.ADD_VOICEMAIL,
    Manifest.permission.USE_SIP
)

val SENSORS = arrayOf(Manifest.permission.BODY_SENSORS)

val SMS = arrayOf(
    Manifest.permission.SEND_SMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.READ_SMS,
    Manifest.permission.RECEIVE_WAP_PUSH,
    Manifest.permission.RECEIVE_MMS
)

val STORAGE = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

fun checkSelfPermission(context: Context, permission: String): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return PackageManager.PERMISSION_GRANTED
    }
    return ContextCompat.checkSelfPermission(context, permission)
}

fun hasPermission(context: Context, permission: String): Boolean {
    return checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun shouldShowRequestPermissionRationale(context: Activity, permission: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return false
    }
    return context.shouldShowRequestPermissionRationale(permission)
}

fun hasDrawOverlayPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return true
    }
    return Settings.canDrawOverlays(context)
}

fun filterGranted(context: Context, permissions: Array<out String>): ArrayList<String> {
    val granted = arrayListOf<String>()
    permissions.forEach {
        if (hasPermission(context, it)) {
            granted.add(it)
        }
    }
    return granted
}

fun filterDenied(context: Context, permissions: Array<out String>): ArrayList<String> {
    val denied = arrayListOf<String>()
    permissions.forEach {
        if (!hasPermission(context, it)) {
            denied.add(it)
        }
    }
    return denied
}

fun filterRationale(context: Activity, permissions: Array<out String>): ArrayList<String> {
    val rationale = arrayListOf<String>()
    permissions.forEach {
        if (shouldShowRequestPermissionRationale(context, it)) {
            rationale.add(it)
        }
    }
    return rationale
}

fun rationale(context: Context, rationale: ArrayList<String>, tips: String = ""): String {
    if (!TextUtils.isEmpty(tips)) {
        return tips
    }
    val toast = transformText(context, rationale)
    var format = context.getString(R.string.permission_message_permission_rationale)
    val sb = StringBuilder()
    toast.forEach {
        sb.append(it).append(", ")
    }
    val permissions = sb.toString()
    if (permissions.isEmpty()) {
        return ""
    }
    return String.format(format, permissions.substring(0, permissions.length - 2))
}

fun appSettings(context: Context, permissions: Array<String>, tips: String = ""): String {
    if (!TextUtils.isEmpty(tips)) {
        return tips;
    }
    val toast = transformText(context, permissions)
    var format = context.getString(R.string.permission_jump_settings_page)
    val sb = StringBuilder()
    toast.forEach {
        sb.append(it).append(", ")
    }
    val permissions1 = sb.toString()
    if (permissions1.isEmpty()) {
        return ""
    }
    return String.format(format, permissions1.substring(0, permissions1.length - 2))
}

/**
 * Turn permissions into text.
 */
fun transformText(context: Context, vararg permissions: String): List<String> {
    return transformText(
        context,
        Arrays.asList(*permissions)
    )
}

/**
 * Turn permissions into text.
 */
fun transformText(context: Context, vararg groups: Array<String>): List<String> {
    val permissionList = ArrayList<String>()
    for (group in groups) {
        permissionList.addAll(listOf(*group))
    }
    return transformText(context, permissionList)
}

/**
 * Turn permissions into text.
 */
fun transformText(context: Context, permissions: List<String>): List<String> {
    val textList = ArrayList<String>()
    for (permission in permissions) {
        when (permission) {
            Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR -> {
                val message = context.getString(R.string.permission_name_calendar)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }

            Manifest.permission.CAMERA -> {
                val message = context.getString(R.string.permission_name_camera)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }
            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS -> {
                val message = context.getString(R.string.permission_name_contacts)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION -> {
                val message = context.getString(R.string.permission_name_location)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }
            Manifest.permission.RECORD_AUDIO -> {
                val message = context.getString(R.string.permission_name_microphone)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.USE_SIP, Manifest.permission.PROCESS_OUTGOING_CALLS -> {
                val message = context.getString(R.string.permission_name_phone)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }
            Manifest.permission.BODY_SENSORS -> {
                val message = context.getString(R.string.permission_name_sensors)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }
            Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_WAP_PUSH,
            Manifest.permission.RECEIVE_MMS -> {
                val message = context.getString(R.string.permission_name_sms)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                val message = context.getString(R.string.permission_name_storage)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }
            Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                val message = context.getString(R.string.permission_name_system_alert)
                if (!textList.contains(message)) {
                    textList.add(message)
                }
            }
        }
    }
    return textList
}