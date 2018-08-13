package com.wzq.test;

/**
 * snowflake的结构:
 * 0-0000000 00000000 00000000 00000000 00000000 00-00000-0 0000-0000 00000000
 * <p>
 * 数据共分为5部分：
 * 1.1位保留0
 * 2.41位毫秒时间
 * 3.5位工作id
 * 4.5位数据中心id
 * 5.12位计数器，到下一毫秒计数器归零
 * <p>
 * 使用位运算存在long类型中
 */
public class Snowflake {

    // 保留字符长度(符号位)
    private final int RETAIN_LENGTH = 1;

    // 时间戳长度(2039-09-07 23:47:35 之前有效，因为这之后的时间戳就是42位了)
    private final int TIME_LENGTH = 41;

    // 工作id长度、最大值(31)
    private final int WORK_ID_LENGTH = 5;
    private final int WORK_ID_MAX = -1 >>> (32 - WORK_ID_LENGTH);

    // 数据中心id长度、最大值(31)
    private final int DATA_CENTER_ID_LENGTH = 5;
    private final int DATA_CENTER_ID_MAX = -1 >>> (32 - DATA_CENTER_ID_LENGTH);

    // 计数器长度、最大值(4095)
    private final int COUNT_LENGTH = 12;
    private final int COUNT_MAX = -1 >>> (32 - COUNT_LENGTH);


    // 当前的工作id、数据中心id
    private long mWorkId;
    private long mDataCenterId;

    // 当前使用的毫秒
    private long mTime;

    // 当前毫秒的计数
    private long mCount;

    public Snowflake(long workId, long dataCenterId) {
        if (workId > WORK_ID_MAX || dataCenterId > DATA_CENTER_ID_MAX) {
            throw new IllegalArgumentException("workId or dataCenterId is bigger than the max value.");
        }

        this.mWorkId = workId;
        this.mDataCenterId = dataCenterId;
    }

    public synchronized long next() {
        // 判断毫秒和计数
        long curTime = System.currentTimeMillis();

        if (mTime == curTime) {
            mCount++;

            // 判断count是否到最大值
            if (mCount > COUNT_MAX) {
                // 则自旋，直到下一毫秒
                while (mTime == curTime) {
                    mTime = System.currentTimeMillis();
                }
                mCount = 0;
            }
        } else {
            mTime = curTime;
            mCount = 0;
        }

        return (mTime << (WORK_ID_LENGTH + DATA_CENTER_ID_LENGTH + COUNT_LENGTH))
                | mWorkId << (DATA_CENTER_ID_LENGTH + COUNT_LENGTH)
                | mDataCenterId << (COUNT_LENGTH)
                | mCount;
    }

}
