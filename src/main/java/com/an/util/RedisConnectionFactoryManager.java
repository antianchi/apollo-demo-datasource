package com.an.util;

import java.util.Collections;
import java.util.List;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.lettuce.core.resource.ClientResources;

@Component
public class RedisConnectionFactoryManager {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RedisConnectionFactoryManager.class);

  @Autowired
  private RedisProperties redisProperties;

  @Autowired
  private ClientResources clientResources;

  @Autowired
  ObjectProvider<List<LettuceClientConfigurationBuilderCustomizer>> builderCustomizers;

  public RedisConnectionFactory createRedisConnectionFactory() {
    // 创建Redistribution连接工厂
    // 获取
    LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
        clientResources, redisProperties.getLettuce().getPool());
    return createLettuceConnectionFactory(clientConfig);
  }
  private LettuceConnectionFactory createLettuceConnectionFactory(
      LettuceClientConfiguration clientConfiguration) {
    // if (getSentinelConfig() != null) {
    // return new LettuceConnectionFactory(getSentinelConfig(),
    // clientConfiguration);
    // }
    // if (getClusterConfiguration() != null) {
    // return new LettuceConnectionFactory(getClusterConfiguration(),
    // clientConfiguration);
    // }
     LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(getStandaloneConfig(),
        clientConfiguration);
     connectionFactory.afterPropertiesSet();
     return connectionFactory;
  }

  protected final RedisStandaloneConfiguration getStandaloneConfig() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    // if (StringUtils.hasText(redisProperties.getUrl())) {
    // ConnectionInfo connectionInfo = parseUrl(redisProperties.getUrl());
    // config.setHostName(connectionInfo.getHostName());
    // config.setPort(connectionInfo.getPort());
    // config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
    // }
    config.setHostName(redisProperties.getHost());
    config.setPort(redisProperties.getPort());
    config.setPassword(RedisPassword.of(redisProperties.getPassword()));
    config.setDatabase(redisProperties.getDatabase());
    System.err.println(redisProperties.getHost());
    System.err.println(redisProperties.getPort());
    System.err.println(redisProperties.getPassword());
    System.err.println(redisProperties.getPassword());
    return config;
  }

  LettuceClientConfiguration getLettuceClientConfiguration(
      ClientResources clientResources, Pool pool) {
    LettuceClientConfigurationBuilder builder = createBuilder(pool);
    applyProperties(builder);
    if (StringUtils.hasText(redisProperties.getUrl())) {
      // customizeConfigurationFromUrl(builder);
    }
    builder.clientResources(clientResources);
    customize(builder);
    return builder.build();
  }

  private LettuceClientConfigurationBuilder createBuilder(Pool pool) {
    if (pool == null) {
      return LettuceClientConfiguration.builder();
    }
    return new PoolBuilderFactory().createBuilder(pool);
  }

  private LettuceClientConfigurationBuilder applyProperties(
      LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
    if (redisProperties.isSsl()) {
      builder.useSsl();
    }
    if (redisProperties.getTimeout() != null) {
      builder.commandTimeout(redisProperties.getTimeout());
    }
    if (redisProperties.getLettuce() != null) {
      RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
      if (lettuce.getShutdownTimeout() != null
          && !lettuce.getShutdownTimeout().isZero()) {
        builder
            .shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout());
      }
    }
    return builder;
  }

  private void customize(
      LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
    for (LettuceClientConfigurationBuilderCustomizer customizer : builderCustomizers
        .getIfAvailable(Collections::emptyList)) {
      customizer.customize(builder);
    }
  }
}
class PoolBuilderFactory {

  public LettuceClientConfigurationBuilder createBuilder(Pool properties) {
    return LettucePoolingClientConfiguration.builder()
        .poolConfig(getPoolConfig(properties));
  }

  private GenericObjectPoolConfig getPoolConfig(Pool properties) {
    GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    config.setMaxTotal(properties.getMaxActive());
    config.setMaxIdle(properties.getMaxIdle());
    config.setMinIdle(properties.getMinIdle());
    if (properties.getMaxWait() != null) {
      config.setMaxWaitMillis(properties.getMaxWait().toMillis());
    }
    return config;
  }
}
