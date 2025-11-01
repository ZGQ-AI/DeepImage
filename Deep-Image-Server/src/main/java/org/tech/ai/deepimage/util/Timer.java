package org.tech.ai.deepimage.util;

/**
 * Simple timer utility class
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
     * Create and start timer
     * 
     * @return Timer instance
     */
    public static Timer start() {
        return new Timer();
    }

    /**
     * Stop timer
     */
    public void stop() {
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
    }

    /**
     * Get elapsed time (milliseconds)
     * If stop() is not called, returns time from creation to current
     * 
     * @return Elapsed time (milliseconds)
     */
    public long getElapsedMillis() {
        long end = endTime != null ? endTime : System.currentTimeMillis();
        return end - startTime;
    }
}
