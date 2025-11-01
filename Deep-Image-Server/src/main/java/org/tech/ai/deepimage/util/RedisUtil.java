package org.tech.ai.deepimage.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis utility class for common operations
 * Encapsulates RedisTemplate operations
 * 
 * @author zgq
 * @since 2025-11-01
 */
@Slf4j
@Component
public class RedisUtil {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Get value from Redis
     * 
     * @param key Cache key
     * @return Optional containing value if exists, empty otherwise
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Optional.of((T) value);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to get value from Redis for key: {}", key, e);
            return Optional.empty();
        }
    }
    
    /**
     * Set value to Redis with TTL
     * 
     * @param key Cache key
     * @param value Value to cache
     * @param ttl Time to live in seconds
     */
    public void set(String key, Object value, long ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            log.debug("Set value to Redis for key: {} with TTL: {} seconds", key, ttl);
        } catch (Exception e) {
            log.warn("Failed to set value to Redis for key: {}", key, e);
        }
    }
    
    /**
     * Delete key from Redis
     * 
     * @param key Cache key
     * @return true if key was deleted, false otherwise
     */
    public boolean delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.warn("Failed to delete key from Redis: {}", key, e);
            return false;
        }
    }
    
    /**
     * Delete multiple keys from Redis by pattern
     * 
     * @param pattern Key pattern (e.g., "deepimage:file:list:123:*")
     * @return Number of keys deleted
     */
    public long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                long count = deleted != null ? deleted : 0;
                log.debug("Deleted {} keys matching pattern: {}", count, pattern);
                return count;
            }
            return 0;
        } catch (Exception e) {
            log.warn("Failed to delete keys by pattern: {}", pattern, e);
            return 0;
        }
    }
    
    /**
     * Check if key exists
     * 
     * @param key Cache key
     * @return true if key exists, false otherwise
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("Failed to check key existence: {}", key, e);
            return false;
        }
    }
    
    /**
     * Get TTL of a key
     * 
     * @param key Cache key
     * @return TTL in seconds, -1 if key doesn't exist, -2 if key exists but has no expiry
     */
    public long getTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -2;
        } catch (Exception e) {
            log.warn("Failed to get TTL for key: {}", key, e);
            return -2;
        }
    }
}

