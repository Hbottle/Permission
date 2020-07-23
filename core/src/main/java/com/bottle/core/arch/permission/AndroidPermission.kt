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
import android.util.Log
import com.bottle.core.R
import com.bottle.core.arch.async.ThreadPoolManager
import com.bottle.core.arch.workflow.OnProcedureListener
import com.bottle.core.arch.workflow.Procedure
import com.bottle.core.arch.workflow.Task
import java.io.Serializable

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

        val androidPermissions = mutableMapOf<Int, AndroidPermission>()
        /**
         * 打开app设置页面
         * @return 由于不同厂商的设置页面可能不一样，不一定适配到，所以如果找不到就返回false
         */
        fun appSettingPage(activity: Activity, requestCode: Int, permissions: Array<String>): Boolean {
            val intent = getAlertWindowSettingIntent(activity)
            if (!resolveActivity(intent, activity)) {
                return false
            }
            Procedure().apply {
                addTask(Task("1") {
                    ThreadPoolManager.main().execute(Runnable {
                        AlertDialog.Builder(activity)
                            .setMessage(
                                appSettings(
                                    activity,
                                    permissions
                                )
                            )
                            .setNegativeButton(activity.getString(R.string.cancel)){
                                    dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                it.cancel()
                            }
                            .setPositiveButton(activity.getString(R.string.ok))
                            { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                it.complete()
                            }
                            .setCancelable(false)
                            .create()
                            .show()
                    })
                })
                addTaskByPreTaskName(arrayOf("1"), Task("2") {
                    ThreadPoolManager.main().execute(Runnable {
                        activity.startActivityForResult(intent, requestCode)
                        it.complete()
                    })
                })
            }.start(object : OnProcedureListener {
                override fun onBlockCompleted(task: Task) {
                    Log.d(AndroidPermission::class.java.simpleName, "onBlockCompleted")
                }

                override fun onBlockFailed(task: Task) {
                    Log.d(AndroidPermission::class.java.simpleName, "onBlockFailed")
                }

                override fun onCompleted() {
                    Log.d(AndroidPermission::class.java.simpleName, "onCompleted")
                }

                override fun onFailed(task: Task) {
                    Log.d(AndroidPermission::class.java.simpleName, "onFailed")
                }

                override fun onCancel(code: Int, reason: String) {
                    Log.d(AndroidPermission::class.java.simpleName, "onCancel")
                }

                override fun onProgress(task: Task, progress: Int, desc: String?) {
                    Log.d(AndroidPermission::class.java.simpleName, "onProgress")
                }
            })
            return true
        }

        /**
         * 打开app悬浮框权限页面，该权限必须要到Settings设置，所以可以先弹框告诉用户，然后再跳转
         * @return 由于不同厂商的设置页面可能不一样，不一定适配到，所以如果找不到就返回false
         */
        fun overlaySettingPage(activity: Activity, requestCode: Int): Boolean {
            val intent = overlaySettingIntent(activity)
            if (!resolveActivity(intent, activity)) {
                return false
            }
            Procedure().apply {
                addTask(Task("1") {
                    ThreadPoolManager.main().execute(Runnable {
                        val permission = arrayListOf(Manifest.permission.SYSTEM_ALERT_WINDOW)
                        AlertDialog.Builder(activity)
                            .setMessage(
                                rationale(
                                    activity,
                                    permission
                                )
                            )
                            .setNegativeButton(activity.getString(R.string.cancel)) { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                it.cancel()
                            }
                            .setPositiveButton(activity.getString(R.string.ok))
                            { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                it.complete()
                            }
                            .setCancelable(false)
                            .create()
                            .show()
                    })
                })
                addTaskByPreTaskName(arrayOf("1"), Task("2") {
                    ThreadPoolManager.main().execute(Runnable {
                        activity.startActivityForResult(intent, requestCode)
                        it.complete()
                    })
                })
            }.start(object : OnProcedureListener {
                override fun onBlockCompleted(task: Task) {
                    Log.d(AndroidPermission::class.java.simpleName, "onBlockCompleted")
                }

                override fun onBlockFailed(task: Task) {
                    Log.d(AndroidPermission::class.java.simpleName, "onBlockFailed")
                }

                override fun onCompleted() {
                    Log.d(AndroidPermission::class.java.simpleName, "onCompleted")
                }

                override fun onFailed(task: Task) {
                    Log.d(AndroidPermission::class.java.simpleName, "onFailed")
                }

                override fun onCancel(code: Int, reason: String) {
                    Log.d(AndroidPermission::class.java.simpleName, "onCancel")
                }

                override fun onProgress(task: Task, progress: Int, desc: String?) {
                    Log.d(AndroidPermission::class.java.simpleName, "onProgress")
                }
            })
            return true
        }

    }

    private lateinit var mPermissions: Array<String>
    private var mOnDenied: (Array<String>) -> Unit = {}
    private var mOnGranted: (Array<String>) -> Unit = {}

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

    fun start(activity: Activity) {
        // TODO 1.Android 6.0以前不需要申请
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mOnGranted.invoke(mPermissions)
            return
        }
        // TODO 2.如果已经获取了权限，那么不需要再申请
        val granted = filterGranted(activity, mPermissions)
        if (granted.size == mPermissions.size) {
            mOnGranted.invoke(mPermissions)
            return
        }
        val rationale = filterRationale(activity, mPermissions)
        // TODO 3.如果是第一次申请权限，则直接申请即可
        if (rationale.isEmpty()) {
            requestPermission(activity)
            return
        }
        // TODO 4.如果需要提示用户(第一次拒绝了，后面每次请求都会提示)为什么申请该权限，则提示
        Procedure().apply {
            addTask(Task("1") { node ->
                ThreadPoolManager.main().execute(Runnable {
                    AlertDialog.Builder(activity)
                        .setMessage(
                            rationale(
                                activity,
                                rationale
                            )
                        )
                        .setPositiveButton(activity.getString(R.string.ok))
                        { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                            node.complete()
                        }
                        .setCancelable(false)
                        .create()
                        .show()
                })
            })
            addTaskByPreTaskName(arrayOf("1"), Task("2") {
                requestPermission(activity)
                it.complete()
            })
        }.start(object : OnProcedureListener {
            override fun onBlockCompleted(task: Task) {
                Log.d(AndroidPermission::class.java.simpleName, "onBlockCompleted")
            }

            override fun onBlockFailed(task: Task) {
                Log.d(AndroidPermission::class.java.simpleName, "onBlockFailed")
            }

            override fun onCompleted() {
                Log.d(AndroidPermission::class.java.simpleName, "onCompleted")
            }

            override fun onFailed(task: Task) {
                Log.d(AndroidPermission::class.java.simpleName, "onFailed")
            }

            override fun onCancel(code: Int, reason: String) {
                Log.d(AndroidPermission::class.java.simpleName, "onCancel")
            }

            override fun onProgress(task: Task, progress: Int, desc: String?) {
                Log.d(AndroidPermission::class.java.simpleName, "onProgress")
            }

        })
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
        // FIXME 请确保hashcode的唯一性
        val hashCode = hashCode()
        intent.putExtra(KEY_ANDROID_PERMISSION, hashCode)
        androidPermissions[hashCode] = this
        mContext.startActivity(intent)
    }

}
