package com.an.util;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import com.an.config.DynamicRedisConnectionFactory;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

/**
 * ClassName: RedisConnectionFactoryRefresher <br/>
 * Function: 刷新redis连接. <br/>
 * Reason: 刷新redis连接. <br/>
 * date: 2019年8月28日 上午9:47:55 <br/>
 *
 * @author atc
 * @version 1.0.0
 * @since JDK 1.8
 */
@Component
public class RedisConnectionFactoryRefresher
    implements
      ApplicationContextAware {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(RedisConnectionFactoryRefresher.class);
  private ApplicationContext applicationContext;

  @Autowired
  private RedisConnectionFactoryManager connectionFactoryManager;

  @Autowired
  private DynamicRedisConnectionFactory dynamicRedisConnectionFactory;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }

  @ApolloConfigChangeListener(interestedKeyPrefixes = {"spring.redis"})
  public void onChange(ConfigChangeEvent changeEvent) {
    refreshRedisConnectionFactory(changeEvent.changedKeys());
  }

  private void refreshRedisConnectionFactory(Set<String> changedKeys) {
    StringBuilder builder = new StringBuilder();
    changedKeys.stream().forEach(r -> {
      builder.append(r).append(",");
    });
    LOGGER.info("刷新REDIS连接-------------》，变更KEYS[{}]", builder.toString());
    /**
     * rebind configuration beans, e.g. DataSourceProperties
     * 
     * @see org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder#onApplicationEvent
     */
    this.applicationContext
        .publishEvent(new EnvironmentChangeEvent(changedKeys));

    RedisConnectionFactory connectionFactory = connectionFactoryManager
        .createRedisConnectionFactory();
    RedisConnectionFactory redisConnectionFactory = dynamicRedisConnectionFactory
        .getAndSet(connectionFactory);
    asyncTerminate(redisConnectionFactory);

    LOGGER.info("Finished refreshing data source");

  }

  // 异步关闭以前的REDIS连接
  private void asyncTerminate(RedisConnectionFactory redisConnectionFactory) {

    redisConnectionFactory.getConnection().close();
  }

}
