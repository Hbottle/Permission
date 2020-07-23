package com.bottle.core.arch.async.executors

import android.os.Process
import com.bottle.core.arch.async.IScheduleExecutor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

/**
 * @Date: 2020/5/7
 * @Author: hugo
 * @Description:
 */

class ScheduledExecutor: IScheduleExecutor {

    private val mScheduledExecutor: ScheduledExecutorService

    @JvmOverloads
    constructor(cpuCount: Int = Runtime.getRuntime().availableProcessors()) {
        mScheduledExecutor = Executors.newScheduledThreadPool(
            2.coerceAtLeast((cpuCount - 1).coerceAtMost(4)),
            ScheduledThreadFactory()
        )
    }

    override fun execute(runnable: Runnable) {
        mScheduledExecutor.execute(runnable)
    }

    override fun schedule(runnable: Runnable, delay: Long) {
        mScheduledExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS)
    }

    override fun shutdown() {
        mScheduledExecutor.shutdown()
    }

    inner class ScheduledThreadFactory : ThreadFactory {

        private var threadCount = 0

        override fun newThread(runnable: Runnable): Thread {
            val thread = Thread(runnable)
            thread.name = "ScheduledThread${threadCount++}"
            thread.priority = Process.THREAD_PRIORITY_BACKGROUND
            return thread
        }

    }

}
