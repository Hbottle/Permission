# Android Permission
用kotlin写的一个运行时权限方便库，简洁而实用。
## 如何使用
可以使用类似Java构建者模式快捷接入；可以使用kotlin自带的构建者模式apply函数快捷接入。
example 1:
```kotlin
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
    Toast.makeText(this@MainActivity, "成功获取权限", Toast.LENGTH_LONG).show()
}
.start(this)
```

example 2:
```kotlin
val permissions = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)
AndroidPermission().apply {
    this.mPermissions = permissions
	// 申请权限前，可以提示用户为什么要用这些权限
    this.mTips = "为了使用相册，请开启SD卡，Camera权限"
    this.mOnDenied = {
	    // 如果没有申请到权限，可以引导用户去设置页面开放权限
        AndroidPermission.appSettingPage(
            this@AlbumActivity, 1010, permissions,
            "缺少必要的权限，app可能无法使用，是否要打开设置页面开启权限？"
        )
    }
    this.mOnGranted = {
        initData()
    }
}.start(this)
```