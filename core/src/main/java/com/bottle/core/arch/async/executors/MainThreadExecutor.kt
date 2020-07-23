package com.bottle.core.arch.async.executors

import android.os.Handler
import android.os.Looper
import com.bottle.core.arch.async.IExecutor

/**
 * @Date: 2020/5/7
 * @Author: hugo
 * @Description: 发送到主线程执行
 */
class MainThreadExecutor : IExecutor {

    private val handler = Handler(Looper.getMainLooper())

    override fun execute(runnable: Runnable) {
        handler.post(runnable)
    }

    override fun shutdown() {
        // do nothing
    }
}
