package org.tech.ai.deepimage.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis Configuration Test
 * Tests Redis connection and basic operations
 * 
 * @author zgq
 * @since 2025-01-XX
 */
@SpringBootTest
class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TEST_KEY_PREFIX = "test:redis:";

    @BeforeEach
    void setUp() {
        // Clean up test keys before each test
        cleanUpTestKeys();
    }

    @AfterEach
    void tearDown() {
        // Clean up test keys after each test
        cleanUpTestKeys();
    }

    /**
     * Clean up all test keys
     */
    private void cleanUpTestKeys() {
        Set<String> keys = redisTemplate.keys(TEST_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * Test basic string operations using StringRedisTemplate
     */
    @Test
    void testStringOperations() {
        String key = TEST_KEY_PREFIX + "string";
        String value = "Hello Redis";

        // Set value
        stringRedisTemplate.opsForValue().set(key, value);
        
        // Get value
        String retrieved = stringRedisTemplate.opsForValue().get(key);
        assertEquals(value, retrieved);

        // Check exists
        Boolean exists = stringRedisTemplate.hasKey(key);
        assertTrue(exists);

        // Delete key
        stringRedisTemplate.delete(key);
        
        // Verify deleted
        exists = stringRedisTemplate.hasKey(key);
        assertFalse(exists);
    }

    /**
     * Test object serialization and deserialization using RedisTemplate
     */
    @Test
    void testObjectOperations() {
        String key = TEST_KEY_PREFIX + "object";
        TestUser user = new TestUser("John", 30, "john@example.com");

        // Set object
        redisTemplate.opsForValue().set(key, user);

        // Get object - using pattern matching would be ideal but RedisTemplate returns Object
        Object retrievedObj = redisTemplate.opsForValue().get(key);
        assertNotNull(retrievedObj);
        assertInstanceOf(TestUser.class, retrievedObj);
        
        TestUser retrieved = (TestUser) retrievedObj;
        assertEquals(user.name(), retrieved.name());
        assertEquals(user.age(), retrieved.age());
        assertEquals(user.email(), retrieved.email());

        // Delete
        redisTemplate.delete(key);
        assertFalse(redisTemplate.hasKey(key));
    }

    /**
     * Test key expiration
     */
    @Test
    void testKeyExpiration() throws InterruptedException {
        String key = TEST_KEY_PREFIX + "expire";
        String value = "Expiring value";

        // Set with expiration (2 seconds)
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(2));

        // Verify exists immediately
        assertTrue(redisTemplate.hasKey(key));
        assertEquals(value, redisTemplate.opsForValue().get(key));

        // Wait for expiration
        Thread.sleep(2100);

        // Verify expired
        assertFalse(redisTemplate.hasKey(key));
        assertNull(redisTemplate.opsForValue().get(key));
    }

    /**
     * Test Hash operations
     */
    @Test
    void testHashOperations() {
        String key = TEST_KEY_PREFIX + "hash";
        // Using Map.of for immutable map creation (Java 9+)
        // Note: Map.of works well with mixed types (String, Integer) as Object values
        Map<String, Object> hashData = Map.of(
            "name", "Alice",
            "age", 25,
            "email", "alice@example.com"
        );

        // Set hash fields
        redisTemplate.opsForHash().putAll(key, hashData);

        // Get all hash fields
        Map<Object, Object> retrieved = redisTemplate.opsForHash().entries(key);
        assertEquals(3, retrieved.size());
        assertEquals("Alice", retrieved.get("name"));
        assertEquals(25, retrieved.get("age"));
        assertEquals("alice@example.com", retrieved.get("email"));

        // Get single field
        Object name = redisTemplate.opsForHash().get(key, "name");
        assertEquals("Alice", name);

        // Delete field
        redisTemplate.opsForHash().delete(key, "email");
        retrieved = redisTemplate.opsForHash().entries(key);
        assertEquals(2, retrieved.size());
        assertFalse(retrieved.containsKey("email"));

        // Delete hash
        redisTemplate.delete(key);
        assertFalse(redisTemplate.hasKey(key));
    }

    /**
     * Test List operations
     */
    @Test
    void testListOperations() {
        String key = TEST_KEY_PREFIX + "list";

        // Push elements
        redisTemplate.opsForList().rightPush(key, "first");
        redisTemplate.opsForList().rightPush(key, "second");
        redisTemplate.opsForList().rightPush(key, "third");

        // Get list size
        Long size = redisTemplate.opsForList().size(key);
        assertEquals(3, size);

        // Get element by index
        Object element = redisTemplate.opsForList().index(key, 0);
        assertEquals("first", element);

        // Get all elements
        Long listSize = redisTemplate.opsForList().size(key);
        assertEquals(3, listSize);

        // Pop element
        Object popped = redisTemplate.opsForList().leftPop(key);
        assertEquals("first", popped);
        assertEquals(2, redisTemplate.opsForList().size(key));

        // Delete list
        redisTemplate.delete(key);
        assertFalse(redisTemplate.hasKey(key));
    }

    /**
     * Test Set operations
     */
    @Test
    void testSetOperations() {
        String key = TEST_KEY_PREFIX + "set";

        // Add elements
        redisTemplate.opsForSet().add(key, "a", "b", "c", "d");

        // Get set size
        Long size = redisTemplate.opsForSet().size(key);
        assertEquals(4, size);

        // Check membership
        Boolean isMember = redisTemplate.opsForSet().isMember(key, "a");
        assertTrue(isMember);
        isMember = redisTemplate.opsForSet().isMember(key, "e");
        assertFalse(isMember);

        // Get all members
        Set<Object> members = redisTemplate.opsForSet().members(key);
        assertEquals(4, members.size());
        assertTrue(members.contains("a"));
        assertTrue(members.contains("b"));

        // Remove member
        redisTemplate.opsForSet().remove(key, "a");
        assertEquals(3, redisTemplate.opsForSet().size(key));

        // Delete set
        redisTemplate.delete(key);
        assertFalse(redisTemplate.hasKey(key));
    }

    /**
     * Test increment and decrement operations
     */
    @Test
    void testIncrementDecrement() {
        String key = TEST_KEY_PREFIX + "counter";

        // Increment
        Long value = stringRedisTemplate.opsForValue().increment(key);
        assertEquals(1, value);

        value = stringRedisTemplate.opsForValue().increment(key);
        assertEquals(2, value);

        // Increment by delta
        value = stringRedisTemplate.opsForValue().increment(key, 5);
        assertEquals(7, value);

        // Decrement
        value = stringRedisTemplate.opsForValue().decrement(key);
        assertEquals(6, value);

        // Decrement by delta
        value = stringRedisTemplate.opsForValue().decrement(key, 3);
        assertEquals(3, value);

        // Delete
        stringRedisTemplate.delete(key);
        assertFalse(stringRedisTemplate.hasKey(key));
    }

    /**
     * Test connection verification
     */
    @Test
    void testConnection() {
        // Verify templates are not null
        assertNotNull(redisTemplate);
        assertNotNull(stringRedisTemplate);

        // Test connection by performing a simple operation
        String testKey = TEST_KEY_PREFIX + "connection";
        stringRedisTemplate.opsForValue().set(testKey, "connected");
        String value = stringRedisTemplate.opsForValue().get(testKey);
        assertEquals("connected", value);
        stringRedisTemplate.delete(testKey);
    }

    /**
     * Test object for serialization testing
     * Using Java 14+ Record feature (stable in Java 17)
     */
    record TestUser(String name, Integer age, String email) {
        // Records automatically provide:
        // - Constructor with all fields
        // - Getters (name(), age(), email())
        // - equals(), hashCode(), toString()
        // - Immutable by default
    }
}

