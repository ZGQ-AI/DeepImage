package org.tech.ai.deepimage.util;

/**
 * 简单的计时器工具类
 * 
 * @author zgq
 * @since 2025-10-22
 */
public class Timer {
    private final long startTime;
    private Long endTime;

    private Timer() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 创建并启动计时器
     * 
     * @return Timer 实例
     */
    public static Timer start() {
        return new Timer();
    }

    /**
     * 停止计时
     */
    public void stop() {
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
    }

    /**
     * 获取已耗时（毫秒）
     * 如果未调用 stop()，返回从创建到当前的时间
     * 
     * @return 耗时（毫秒）
     */
    public long getElapsedMillis() {
        long end = endTime != null ? endTime : System.currentTimeMillis();
        return end - startTime;
    }
}
