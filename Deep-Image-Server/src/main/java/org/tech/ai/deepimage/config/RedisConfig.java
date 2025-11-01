package org.tech.ai.deepimage.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration Class
 * Configures RedisTemplate and StringRedisTemplate for Redis operations
 * 
 * @author zgq
 * @since 2025-01-XX
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * Configure RedisTemplate with JSON serialization
     * Supports serialization of Java objects to JSON format
     * 
     * @param connectionFactory Redis connection factory
     * @return RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use JSON serializer for values (compatible with fastjson2)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("RedisTemplate configured successfully");
        return template;
    }

    /**
     * Configure StringRedisTemplate for string operations
     * Optimized for string-only operations
     * 
     * @param connectionFactory Redis connection factory
     * @return StringRedisTemplate instance
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        log.info("StringRedisTemplate configured successfully");
        return template;
    }

    /**
     * Custom JSON serializer using fastjson2
     * Provides better performance and compatibility with existing codebase
     */
    private static class GenericJackson2JsonRedisSerializer implements RedisSerializer<Object> {
        
        @Override
        public byte[] serialize(Object obj) throws SerializationException {
            if (obj == null) {
                return new byte[0];
            }
            try {
                return JSON.toJSONBytes(obj, JSONWriter.Feature.WriteClassName);
            } catch (Exception e) {
                throw new SerializationException("Failed to serialize object to JSON: " + obj.getClass().getName(), e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                // Parse with type auto-detection enabled
                // When WriteClassName is used during serialization, fastjson2 will restore the original type
                // Note: SupportAutoType is used here to enable type auto-detection from @type field
                // This is necessary for proper deserialization of custom objects
                return JSON.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
            } catch (Exception e) {
                throw new SerializationException("Failed to deserialize JSON to object", e);
            }
        }
    }
}

