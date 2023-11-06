package net.fileme.utils;

import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.InternalErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
//@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class RedisCache {
    @Autowired
    private RedisTemplate redisTemplate;

    public <T> T getRedisValue(String key){
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }
    public <T>  boolean setUniqueObj(String key, T value){
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }
    public <T>  boolean setUniqueObj(String key, T value, Integer timeout, TimeUnit timeUnit){
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }
    public <T>  void setObj(String key, T value){
        redisTemplate.opsForValue().set(key, value);
    }
    public <T>  void setObj(String key, T value, Integer timeout, TimeUnit timeUnit){
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }
    public boolean hasKey(String key){
        return redisTemplate.hasKey(key);
    }
    public void deleteObj(String key){
        Boolean success = redisTemplate.delete(key);
        if(!success)
            throw new InternalErrorException(ExceptionEnum.REDIS_DEL_ERROR);
    }
}
