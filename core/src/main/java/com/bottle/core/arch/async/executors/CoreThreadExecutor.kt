package com.bottle.core.arch.async.executors

import android.os.Process
import com.bottle.core.arch.async.IExecutor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @Date: 2020/5/7
 * @Author: hugo
 * @Description:
 */
class CoreThreadExecutor : IExecutor {

    private val mCoreThreadExecutor: ThreadPoolExecutor

    @JvmOverloads
    constructor(cpuCount: Int = Runtime.getRuntime().availableProcessors()) {
        mCoreThreadExecutor = ThreadPoolExecutor(
            2.coerceAtLeast((cpuCount - 1).coerceAtMost(4)),
            cpuCount * 2 + 1,
            30L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            CoreThreadFactory(),
            ThreadPoolExecutor.CallerRunsPolicy())
    }

    override fun execute(runnable: Runnable) {
        mCoreThreadExecutor.execute(runnable)
    }

    override fun shutdown() {
        mCoreThreadExecutor.shutdown()
    }

    inner class CoreThreadFactory : ThreadFactory {

        private var threadCount = 0

        override fun newThread(runnable: Runnable): Thread {
            val thread = Thread(runnable)
            thread.name = "Thread-${threadCount++}"
            thread.priority = Process.THREAD_PRIORITY_BACKGROUND
            // A exception handler is created to log the exception from threads
            // thread.setUncaughtExceptionHandler(handler)
            return thread
        }

    }
}

