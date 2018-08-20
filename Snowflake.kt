package com.wzq.test

/**
 * snowflake的结构:
 * 0-0000000 00000000 00000000 00000000 00000000 00-00000-0 0000-0000 00000000
 *
 *
 * 数据共分为5部分：
 * 1.1位保留0
 * 2.41位毫秒时间
 * 3.5位工作id
 * 4.5位数据中心id
 * 5.12位计数器，到下一毫秒计数器归零
 *
 *
 * 使用位运算存在long类型中
 */
class Snowflake(// 当前的工作id、数据中心id
        private val mWorkId: Long, private val mDataCenterId: Long) {

    // 保留字符长度(符号位)
    private val RETAIN_LENGTH = 1

    // 时间戳长度(2039-09-07 23:47:35 之前有效，因为这之后的时间戳就是42位了)
    private val TIME_LENGTH = 41

    // 工作id长度、最大值(31)
    private val WORK_ID_LENGTH = 5
    private val WORK_ID_MAX = (-1).ushr(32 - WORK_ID_LENGTH)

    // 数据中心id长度、最大值(31)
    private val DATA_CENTER_ID_LENGTH = 5
    private val DATA_CENTER_ID_MAX = (-1).ushr(32 - DATA_CENTER_ID_LENGTH)

    // 计数器长度、最大值(4095)
    private val COUNT_LENGTH = 12
    private val COUNT_MAX = (-1).ushr(32 - COUNT_LENGTH)

    // 当前使用的毫秒
    private var mTime: Long = 0

    // 当前毫秒的计数
    private var mCount: Long = 0

    init {
        if (mWorkId > WORK_ID_MAX || mDataCenterId > DATA_CENTER_ID_MAX) {
            throw IllegalArgumentException("workId or dataCenterId is bigger than the max value.")
        }
    }

    @Synchronized
    operator fun next(): Long {
        // 判断毫秒和计数
        val curTime = System.currentTimeMillis()

        if (mTime == curTime) {
            mCount++

            // 判断count是否到最大值
            if (mCount > COUNT_MAX) {
                // 则自旋，直到下一毫秒
                while (mTime == curTime) {
                    mTime = System.currentTimeMillis()
                }
                mCount = 0
            }
        } else {
            mTime = curTime
            mCount = 0
        }

        return (mTime shl WORK_ID_LENGTH + DATA_CENTER_ID_LENGTH + COUNT_LENGTH
                or (mWorkId shl DATA_CENTER_ID_LENGTH + COUNT_LENGTH)
                or (mDataCenterId shl COUNT_LENGTH)
                or mCount)
    }

}
