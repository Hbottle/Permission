package com.bottle.core.arch.permission

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

const val KEY_BUNDLE = "bundle"
const val KEY_PERMISSIONS = "permissions"
const val KEY_ANDROID_PERMISSION = "android_permission"
const val CODE_PERMISSION = 101

class PermissionActivity : AppCompatActivity() {

    private lateinit var mPermissions: Array<String>
    private var androidPermissionHashCode: String = ""
    private var mPermissionHelper: PermissionHelper? = null

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
            mPermissionHelper?.granted(mPermissions)
        } else {
            mPermissionHelper?.denied(denied.toTypedArray())
        }
        mPermissionHelper = null
        PermissionHelper.androidPermissions.remove(androidPermissionHashCode)
        finish()
    }

    private fun initBundle(bundle: Bundle) {
        mPermissions = bundle[KEY_PERMISSIONS] as Array<String>
        androidPermissionHashCode = intent.getStringExtra(KEY_ANDROID_PERMISSION)!!
        if (!TextUtils.isEmpty(androidPermissionHashCode)) {
            mPermissionHelper = PermissionHelper.androidPermissions[androidPermissionHashCode]
        }
        require(!mPermissions.isNullOrEmpty()) { "there have no permission!" }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, mPermissions, CODE_PERMISSION)
        } else {
            mPermissionHelper = null
            PermissionHelper.androidPermissions.remove(androidPermissionHashCode)
            finish()
        }
    }
}
