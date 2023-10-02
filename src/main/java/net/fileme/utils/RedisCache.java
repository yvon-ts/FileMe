package net.fileme.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class RedisCache {
    @Autowired
    private RedisTemplate redisTemplate;

    public <T> void setCacheObject(final String key, final T value){
        redisTemplate.opsForValue().set(key, value);
    }
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit){
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }
}
