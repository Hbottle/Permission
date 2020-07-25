package com.bottle.permission

import android.Manifest
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.bottle.core.arch.permission.PermissionHelper
import com.bottle.core.arch.permission.Permissions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val permissions = Permissions.STORAGE + Permissions.CAMERA + Permissions.AUDIO
        PermissionHelper()
            .permission(permissions)
            .onDenied {
                // 如果没有同意，询问是否要去设置页，再次打开权限
                PermissionHelper.appSettingPage(this@MainActivity, 1010, permissions)
            }
            .onGranted {
                Toast.makeText(this@MainActivity, "成功获取权限", Toast.LENGTH_SHORT).show()
            }
            .showTipsOnFirstTime(true) // default is true
            .tips("为了正常使用app，需要您同意app使用存储权限和相机权限")
            .start(this)

//        val permissions = arrayOf(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO
//        )
//        PermissionHelper().apply {
//            this.mPermissions = permissions
//            this.mOnDenied = {
//                PermissionHelper.appSettingPage(this@MainActivity, 1010, permissions)
//            }
//            this.mOnGranted = {
//                Toast.makeText(this@MainActivity, "成功获取权限", Toast.LENGTH_SHORT).show()
//            }
//            this.showTipsOnFirstTime = true
//            this.mTips = "为了正常使用app，需要您同意app使用存储权限、相机权限和录音权限"
//        }.start(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
    }
}