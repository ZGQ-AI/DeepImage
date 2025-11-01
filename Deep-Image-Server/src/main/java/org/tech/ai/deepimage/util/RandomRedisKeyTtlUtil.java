package org.tech.ai.deepimage.util;

import java.util.Random;

/**
 * Utility class for generating random TTL for Redis keys
 * Prevents cache stampede by spreading expiration times
 * 
 * @author zgq
 * @since 2025-11-01
 */
public class RandomRedisKeyTtlUtil {
    
    private static final int MIN_TTL_SECONDS = 60;      // 1 minute
    private static final int MAX_TTL_SECONDS = 300;     // 5 minutes
    private static final int BASE_TTL_SECONDS = 180;    // 3 minutes (base TTL)
    private static final int TTL_OFFSET_RANGE = 120;    // ±120 seconds
    
    private static final Random random = new Random();
    
    /**
     * Calculate random TTL within [60, 300] seconds range
     * Formula: baseTtl(180) + random(-120, +120), clamped to [60, 300]
     * 
     * This prevents cache stampede by spreading expiration times across a 4-minute window
     * 
     * @return Random TTL in seconds, clamped to [60, 300]
     */
    public static int calculateRandomTTL() {
        // Generate random offset: -120 to +120 seconds
        int offset = random.nextInt(TTL_OFFSET_RANGE * 2 + 1) - TTL_OFFSET_RANGE;
        
        // Calculate final TTL
        int ttl = BASE_TTL_SECONDS + offset;
        
        // Clamp to [60, 300] seconds
        return Math.max(MIN_TTL_SECONDS, Math.min(MAX_TTL_SECONDS, ttl));
    }
    
    /**
     * Calculate random TTL with custom base TTL
     * Formula: baseTtl + random(-120, +120), clamped to [60, 300]
     * 
     * @param baseTtlSeconds Base TTL in seconds
     * @return Random TTL in seconds, clamped to [60, 300]
     */
    public static int calculateRandomTTL(int baseTtlSeconds) {
        // Generate random offset: -120 to +120 seconds
        int offset = random.nextInt(TTL_OFFSET_RANGE * 2 + 1) - TTL_OFFSET_RANGE;
        
        // Calculate final TTL
        int ttl = baseTtlSeconds + offset;
        
        // Clamp to [60, 300] seconds
        return Math.max(MIN_TTL_SECONDS, Math.min(MAX_TTL_SECONDS, ttl));
    }
    
    /**
     * Get minimum TTL value
     * 
     * @return Minimum TTL in seconds (60)
     */
    public static int getMinTtl() {
        return MIN_TTL_SECONDS;
    }
    
    /**
     * Get maximum TTL value
     * 
     * @return Maximum TTL in seconds (300)
     */
    public static int getMaxTtl() {
        return MAX_TTL_SECONDS;
    }
    
    /**
     * Get base TTL value
     * 
     * @return Base TTL in seconds (180)
     */
    public static int getBaseTtl() {
        return BASE_TTL_SECONDS;
    }
}

