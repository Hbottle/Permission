package com.bottle.core.arch.async

/**
 * @Date: 2020/5/7
 * @Author: hugo
 * @Description:
 */
interface IExecutor {
    fun execute(runnable: Runnable)

    fun shutdown()
}
