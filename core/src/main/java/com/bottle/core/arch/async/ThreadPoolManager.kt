package com.bottle.core.arch.async

import com.bottle.core.arch.async.executors.CoreThreadExecutor
import com.bottle.core.arch.async.executors.SingleThreadExecutor
import com.bottle.core.arch.async.executors.MainThreadExecutor
import com.bottle.core.arch.async.executors.ScheduledExecutor

/**
 * @Date: 2020/5/7
 * @Author: hugo
 * @Description: App 异步任务，建议统一使用此工具类，避免直接 new Thread 或者随意使用ThreadPoolExecutor
 * 一个线程管理工具，任务可以安排在以下四个线程中执行
 * 1.主线程
 * 2.io线程(io操作只需要)
 * 3.计划线程(可以delay)
 * 4.核心线程
 */
class ThreadPoolManager {

    companion object {

        private var mMainThreadExecutor: MainThreadExecutor? = null
        private var mSingleThreadExecutor: SingleThreadExecutor? = null
        private var mCoreThreadExecutor: CoreThreadExecutor? = null
        private var mScheduledExecutor: ScheduledExecutor? = null

        @Synchronized
        fun main() : IExecutor {
            if (mMainThreadExecutor == null) {
                mMainThreadExecutor =
                    MainThreadExecutor()
            }
            return mMainThreadExecutor!!
        }

        @Synchronized
        fun io() : IExecutor {
            if (mSingleThreadExecutor == null) {
                mSingleThreadExecutor =
                    SingleThreadExecutor()
            }
            return mSingleThreadExecutor!!
        }

        @Synchronized
        fun core() : IExecutor {
            if (mCoreThreadExecutor == null) {
                mCoreThreadExecutor =
                    CoreThreadExecutor()
            }
            return mCoreThreadExecutor!!
        }

        @Synchronized
        fun scheduled() : IScheduleExecutor {
            if (mScheduledExecutor == null) {
                mScheduledExecutor =
                    ScheduledExecutor()
            }
            return mScheduledExecutor!!
        }
    }

}
