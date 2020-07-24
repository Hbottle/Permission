package com.bottle.core.arch.permission

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.MainThread
import com.bottle.core.R
import java.io.Serializable
import java.util.UUID

internal val MARK = Build.MANUFACTURER.toLowerCase()

/**
 * Android 运行时权限，参考了<a href="https://github.com/yanzhenjie/AndroidPermission">AndroidPermission</a>
 * 还有<a href="https://mp.weixin.qq.com/s/sKRe_jyFBYEDIKdgjz7rZQ">鸿神的这篇博客</a>
 * 之所以重复造一个轮子：
 * 第一AndPermission是Java，我要写的是Kotlin；
 * 第二，AndPermission有点复杂，希望可以简化一下；
 * 第三，自己写比较可控一些，有什么要改也的方便。
 * 原理比较简单，申请权限时打开一个透明的Activity，在这个Activity里面申请，收到onRequestPermissionsResult
 * 回调后，再处理结果，然后通过回调告知调用者是结果(onGranted or onDenied)。
 *
 * example 1:
 *
val permissions = arrayOf(
Manifest.permission.CAMERA,
Manifest.permission.RECORD_AUDIO,
Manifest.permission.WRITE_EXTERNAL_STORAGE,
Manifest.permission.READ_EXTERNAL_STORAGE
)
AndroidPermission()
.permission(permissions)
.onDenied {
AndroidPermission.appSettingPage(this, 1010, permissions)
}
.onGranted {
initData()
}
.start(this)

 example 2:
AndroidPermission().apply {
this.mPermissions = permissions
this.mTips = "为了使用相册，请开启SD卡，Camera权限"
this.mOnDenied = {
AndroidPermission.appSettingPage(
this@AlbumActivity, 1010, permissions,
"缺少必要的权限，app可能无法使用，是否要打开设置页面开启权限？"
)
}
this.mOnGranted = {
initData()
}
}.start(this)
 */

fun resolveActivity(intent: Intent, context: Context): Boolean {
    return intent.resolveActivity(context.packageManager) != null
}

fun hasActivity(context: Context, intent: Intent): Boolean {
    val packageManager = context.packageManager
    return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size > 0
}

class AndroidPermission() : Serializable {

    companion object {

        val androidPermissions = mutableMapOf<String, AndroidPermission>()

        /**
         * 打开app设置页面
         * @return 由于不同厂商的设置页面可能不一样，不一定适配到，所以如果找不到就返回false
         */
        @MainThread
        fun appSettingPage(
            ac: Activity,
            requestCode: Int,
            permissions: Array<String>,
            tips: String = ""
        ): Boolean {
            val intent = getAlertWindowSettingIntent(ac)
            if (!resolveActivity(intent, ac)) {
                return false
            }
            AlertDialog.Builder(ac)
                .setMessage(appSettings(ac, permissions, tips))
                .setNegativeButton(ac.getString(R.string.cancel)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .setPositiveButton(ac.getString(R.string.ok)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    ac.startActivityForResult(intent, requestCode)
                }
                .setCancelable(false)
                .create()
                .show()
            return true
        }

        /**
         * 打开app悬浮框权限页面，该权限必须要到Settings设置，所以可以先弹框告诉用户，然后再跳转
         * @return 由于不同厂商的设置页面可能不一样，不一定适配到，所以如果找不到就返回false
         */
        @MainThread
        fun overlaySettingPage(activity: Activity, requestCode: Int): Boolean {
            val intent = overlaySettingIntent(activity)
            if (!resolveActivity(intent, activity)) {
                return false
            }
            val permission = arrayListOf(Manifest.permission.SYSTEM_ALERT_WINDOW)
            AlertDialog.Builder(activity)
                .setMessage(rationale(activity, permission))
                .setNegativeButton(activity.getString(R.string.cancel)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .setPositiveButton(activity.getString(R.string.ok)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    activity.startActivityForResult(intent, requestCode)
                }
                .setCancelable(false)
                .create()
                .show()
            return true
        }

    }

    lateinit var mPermissions: Array<String>

    /**
     * 提示用户，为什么要用这些权限(建议使用自定义的提示，内置的提示没有翻译)
     */
    var mTips: String = ""
    var mOnDenied: (Array<String>) -> Unit = {}
    var mOnGranted: (Array<String>) -> Unit = {}

    /**
     * 申请权限时，是否显示提示(mTips)
     */
    var showTipsOnFirstTime: Boolean = true

    fun permission(permissions: Array<String>): AndroidPermission {
        mPermissions = permissions
        return this
    }

    fun onGranted(action: (Array<String>) -> Unit): AndroidPermission {
        mOnGranted = action
        return this
    }

    fun onDenied(action: (Array<String>) -> Unit): AndroidPermission {
        mOnDenied = action
        return this
    }

    fun tips(tips: String): AndroidPermission {
        mTips = tips
        return this
    }

    fun showTipsOnFirstTime(showOnFirst: Boolean): AndroidPermission {
        this.showTipsOnFirstTime = showOnFirst
        return this
    }

    @MainThread
    fun start(activity: Activity) {
        // 1.Android 6.0以前不需要申请
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mOnGranted.invoke(mPermissions)
            return
        }
        // 2.如果已经获取了权限，那么不需要再申请
        val granted = filterGranted(activity, mPermissions)
        if (granted.size == mPermissions.size) {
            mOnGranted.invoke(mPermissions)
            return
        }
        val rationale = filterRationale(activity, mPermissions)
        // 3.如果是第一次申请权限，则直接申请即可
        if (rationale.isEmpty() && !showTipsOnFirstTime) {
            requestPermission(activity)
            return
        }
        if (rationale.isEmpty()) {
            for (p in mPermissions) {
                rationale.add(p)
            }
        }
        // 4.如果需要提示用户(第一次拒绝了，后面每次请求都会提示)为什么申请该权限，则提示
        AlertDialog.Builder(activity)
            .setMessage(rationale(activity, rationale, mTips))
            .setPositiveButton(activity.getString(R.string.ok)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                requestPermission(activity)
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    internal fun granted(permissions: Array<String>) {
        mOnGranted.invoke(permissions)
    }

    internal fun denied(permissions: Array<String>) {
        mOnDenied.invoke(permissions)
    }

    private fun requestPermission(mContext: Activity) {
        val intent = Intent(mContext, PermissionActivity::class.java)
        val data = Bundle()
        data.putSerializable(KEY_PERMISSIONS, mPermissions)
        intent.putExtra(KEY_BUNDLE, data)
        val hashCode = UUID.randomUUID().toString()
        intent.putExtra(KEY_ANDROID_PERMISSION, hashCode)
        androidPermissions[hashCode] = this
        mContext.startActivity(intent)
    }

}
