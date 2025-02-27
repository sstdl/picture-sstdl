package com.sstdl.picturebackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author SSTDL
 * @description
 */
@SpringBootTest
public class RedisStringTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test(){
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();

        String key = "testKey";
        String value = "testValue";

        valueOperations.set(key, value);
        System.out.println(valueOperations.get(key));

        String updateValue = "testUpload";
        valueOperations.set(key, updateValue);
        System.out.println(valueOperations.get(key));

        String s = valueOperations.get(key);
        System.out.println(s);

        stringRedisTemplate.delete(key);
        System.out.println(valueOperations.get(key));

    }
}
