package com.an.util;

import java.net.URI;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration.PoolBuilderFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionConfiguration.ConnectionInfo;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

import io.lettuce.core.resource.ClientResources;

@Component
public class RedisConnectionFactoryManager {


  private static final Logger LOGGER = LoggerFactory
      .getLogger(RedisConnectionFactoryManager.class);


  @Autowired
  private RedisProperties redisProperties;
  
  @Autowired
  private ClientResources clientResources;

  public RedisConnectionFactory createRedisConnectionFactory() {
    // 创建Redistribution连接工厂
    // 获取
    LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
        clientResources, redisProperties.getLettuce().getPool());
    return new LettuceConnectionFactory(getStandaloneConfig(), clientConfiguration);    
  }
  
  private LettuceClientConfiguration getLettuceClientConfiguration(
      ClientResources clientResources, Pool pool) {
    LettuceClientConfigurationBuilder builder = createBuilder(pool);
    applyProperties(builder);
    if (StringUtils.hasText(redisProperties.getUrl())) {
      customizeConfigurationFromUrl(builder);
    }
    builder.clientResources(clientResources);
    customize(builder);
    return builder.build();
  }
  private void customize(
      LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
    for (LettuceClientConfigurationBuilderCustomizer customizer : this.builderCustomizers) {
      customizer.customize(builder);
    }
  }
  private void customizeConfigurationFromUrl(
      LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
    ConnectionInfo connectionInfo = parseUrl(this.properties.getUrl());
    if (connectionInfo.isUseSsl()) {
      builder.useSsl();
    }
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
        builder.shutdownTimeout(
            redisProperties.getLettuce().getShutdownTimeout());
      }
    }
    return builder;
  }
  
  private LettuceClientConfigurationBuilder createBuilder(Pool pool) {
    if (pool == null) {
      return LettuceClientConfiguration.builder();
    }
    return new PoolBuilderFactory().createBuilder(pool);
  }
  
  /**
   * Inner class to allow optional commons-pool2 dependency.
   */
  private static class PoolBuilderFactory {

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
  protected static class ConnectionInfo {

    private final URI uri;

    private final boolean useSsl;

    private final String password;

    public ConnectionInfo(URI uri, boolean useSsl, String password) {
      this.uri = uri;
      this.useSsl = useSsl;
      this.password = password;
    }

    public boolean isUseSsl() {
      return this.useSsl;
    }

    public String getHostName() {
      return this.uri.getHost();
    }

    public int getPort() {
      return this.uri.getPort();
    }

    public String getPassword() {
      return this.password;
    }

  }

}

