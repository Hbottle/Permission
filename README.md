# Permission

## 概述

Android系统不断更新，对隐私安全方面的控制越来越强。因为隐私安全条款不断更新，有时候会因为运行时权限专门发一个app补丁版本。
原生申请运行时权限相对来说较为麻烦，而且都是模板代码，如果直接使用，则需要重复大量的模板代码。所以，有必要为运行时权限申请
写一个方便工具。github中不乏现成的优秀的运行时权限项目，如：[AndPermission](https://github.com/yanzhenjie/AndPermission)，
还有一些甚至使用了AOP来实现的。那么为什么还要重复造轮子？不是开玩笑，主要是因为闲的，时间允许的情况下，这种简单的工具没必要
引入一个额外的依赖，同时通过阅读别人的代码和动手实践，可以更好的理解，万一以后有什么要改的地方，也可以从容不迫。所以，在阅读了
[AndPermission](https://github.com/yanzhenjie/AndPermission)项目后，用kotlin写的一个运行时权限方便工具，简洁而实用。

## 运行时权限申请流程

首先，梳理一下在工作中，是如何处理运行时权限的：
1. 当需要某个权限时，先向提示用户，为什么要用这些权限；
2. 如果用户无视这个提示，取消权限申请，则流程结束，同时，后面的业务受阻；
3. 如果用户同意申请权限，则发起申请权限的流程；
4. 获取权限流程返回成功，则继续后面的业务；
5. 获取权限流程返回失败，则提示并引导用户到设置页面去授权；

## 如何实现

实现原理比较简单，因为申请权限只能在Activity中进行，所以申请权限时打开一个透明的Activity，在这个Activity里面申请
（因为这个缘故，如果在Activity的onResume中申请，而用户又一直不同意，则可能导致一直提示申请权限），
收到 onRequestPermissionsResult 回调后，再处理结果，然后通过回调告知调用者是结果(onGranted or onDenied)。


在申请权限时，会先检查这些权限的现状：
1. 用户已经获得所申请的全部权限，则直接调用 onGranted 返回成功；
2. 如果用户获得所申请的部分权限，则提示用户为什么需要这些权限，用户点击确定，则开始申请权限的流程，否则调用onDenied；
3. 如果用户未获得所申请的任何权限，如果是第一次申请，则可以选择是否提示为什么要获取权限，然后走申请权限的流程；
4. 如果用户未授权部分/全部权限，则在onDenied返回中，可以引导用户到“设置”页去授权。

## 如何使用

1. 可以使用类似Java构建者模式快捷接入；
2. 可以使用kotlin自带的构建者模式apply函数快捷接入。

示例代码 1:

```kotlin
val permissions = Permissions.STORAGE + Permissions.CAMERA + Permissions.AUDIO
PermissionHelper()
.permission(permissions)
.onDenied {
    // 如果没有同意，询问是否要去设置页，再次打开权限，或者提示用户后面的业务无法进行，然后退出
    PermissionHelper.appSettingPage(this@MainActivity, 1010, permissions)
}
.onGranted {
    // 获得权限，可以进行后面的业务
    Toast.makeText(this@MainActivity, "成功获取权限", Toast.LENGTH_SHORT).show()
}
.showTipsOnFirstTime(true) // default is true
.tips("为了正常使用app，需要您同意app使用存储权限、相机权限和录音权限")
.start(this)
```

示例代码 2:

```kotlin
val permissions = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO
)
PermissionHelper().apply {
    this.mPermissions = permissions
    this.mOnDenied = {
        PermissionHelper.appSettingPage(this@MainActivity, 1010, permissions)
    }
    this.mOnGranted = {
        Toast.makeText(this@MainActivity, "成功获取权限", Toast.LENGTH_SHORT).show()
    }
    this.showTipsOnFirstTime = true
    this.mTips = "为了正常使用app，需要您同意app使用存储权限、相机权限和录音权限"
}.start(this)
```

## 感谢
感谢[AndPermission](https://github.com/yanzhenjie/AndPermission)，本项目原理和部分代码是从这个项目抄过来的，
如提示提示用户为什么需要这些权限的string资源，和组装提示的代码。但是后面改成了允许用户自定义提示，毕竟这样就可以
把翻译的工作交给调用的项目了。如果调用者不传提示，还是会使用这些组装的提示。





