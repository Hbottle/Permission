package com.bottle.core.arch.async.executors

import android.os.Process
import com.bottle.core.arch.async.IExecutor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

/**
 * @Date: 2020/5/7
 * @Author: hugo
 * @Description: 只有一个线程，可用于io
 */
class SingleThreadExecutor : IExecutor {

    private val mIoThreadExecutor = Executors.newSingleThreadExecutor(SingleThreadFactory())

    override fun execute(runnable: Runnable) {
        mIoThreadExecutor.execute(runnable)
    }

    override fun shutdown() {
        mIoThreadExecutor.shutdown()
    }

    inner class SingleThreadFactory : ThreadFactory {

        override fun newThread(r: Runnable): Thread {
            val thread = Thread(r)
            thread.name = "SingleThread"
            thread.priority = Process.THREAD_PRIORITY_BACKGROUND
            return thread
        }

    }
}


