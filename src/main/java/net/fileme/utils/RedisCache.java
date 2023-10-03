package net.fileme.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class RedisCache {
    @Autowired
    private RedisTemplate redisTemplate;

    public<T> String setUniqueKey(final T value, final Integer timeout, final TimeUnit timeUnit){
        String key = "";
        boolean isUnique = false;
        while(!isUnique){
            key = RandomUtil.createToken();
            isUnique = setCacheObject(key, value, timeout, timeUnit);
        }
        return key;
    }

    public <T> boolean setCacheObject(final String key, final T value){
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }
    public <T> boolean setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit){
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }

    public <T> T getCacheObject(final String key){
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    public boolean hasKey(final String key){
        return redisTemplate.hasKey(key);
    }
    public boolean deleteObject(final String key){
        return redisTemplate.delete(key);
    }
}
