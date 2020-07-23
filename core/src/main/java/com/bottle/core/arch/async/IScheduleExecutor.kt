package com.bottle.core.arch.async

/**
 * @Date: 2020/5/7
 * @Author: hugo
 * @Description:
 */
interface IScheduleExecutor : IExecutor {
    fun schedule(runnable: Runnable, delay: Long){}
}
