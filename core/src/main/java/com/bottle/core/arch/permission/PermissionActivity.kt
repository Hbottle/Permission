package com.bottle.core.arch.permission

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

const val KEY_BUNDLE = "bundle"
const val KEY_PERMISSIONS = "permissions"
const val KEY_ANDROID_PERMISSION = "android_permission"
const val CODE_PERMISSION = 101

class PermissionActivity : AppCompatActivity() {

    private lateinit var mPermissions: Array<String>
    private var androidPermissionHashCode: Int = -1
    private var mAndroidPermission: AndroidPermission? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            initBundle(savedInstanceState)
        } else {
            val bundle = intent.getBundleExtra(KEY_BUNDLE) ?: return
            initBundle(bundle)
        }
        requestPermissions()
    }

    override fun onRequestPermissionsResult(code: Int, arg: Array<out String>, result: IntArray) {
        if (CODE_PERMISSION != code) {
            return
        }
        val denied = filterDenied(this, arg)
        if (denied.isEmpty()) {
            mAndroidPermission?.granted(mPermissions)
        } else {
            mAndroidPermission?.denied(denied.toTypedArray())
        }
        mAndroidPermission = null
        AndroidPermission.androidPermissions.remove(androidPermissionHashCode)
        finish()
    }

    private fun initBundle(bundle: Bundle) {
        mPermissions = bundle[KEY_PERMISSIONS] as Array<String>
        androidPermissionHashCode = intent.getIntExtra(KEY_ANDROID_PERMISSION, -1)
        if (androidPermissionHashCode != -1) {
            mAndroidPermission = AndroidPermission.androidPermissions[androidPermissionHashCode]
        }
        require(!mPermissions.isNullOrEmpty()) { "there have no permission!" }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, mPermissions, CODE_PERMISSION)
        } else {
            mAndroidPermission = null
            AndroidPermission.androidPermissions.remove(androidPermissionHashCode)
            finish()
        }
    }
}
