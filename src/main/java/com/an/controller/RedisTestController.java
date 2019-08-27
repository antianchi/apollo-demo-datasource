package com.an.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: RedisTestController <br/>
 * Function: Redis测试控制器. <br/>
 * Reason: Redis测试控制器. <br/>
 * date: 2019年8月27日 下午7:18:54 <br/>
 *
 * @author atc
 * @version 1.0.0
 * @since JDK 1.8
 */
@RestController
public class RedisTestController {

  @Autowired
  private StringRedisTemplate redisTemplate;
  
  @GetMapping("/getval/{key}")
  private Object get(@PathVariable("key") String key) {
    RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
    System.out.println(connectionFactory.getClass().getName());
    return redisTemplate.opsForValue().get(key);
  }
}

