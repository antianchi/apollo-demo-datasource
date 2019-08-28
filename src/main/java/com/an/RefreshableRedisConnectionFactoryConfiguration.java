package com.an;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.an.config.DynamicRedisConnectionFactory;
import com.an.util.RedisConnectionFactoryManager;

@Configuration
public class RefreshableRedisConnectionFactoryConfiguration {

  @Bean
  public DynamicRedisConnectionFactory redisConnectionFactory(
      RedisConnectionFactoryManager redisConnectionFactoryManager) {
    RedisConnectionFactory redisConnectionFactory = redisConnectionFactoryManager
        .createRedisConnectionFactory();
    return new DynamicRedisConnectionFactory(redisConnectionFactory);

  }
}
