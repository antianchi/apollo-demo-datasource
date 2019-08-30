package com.an.config;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;

public class DynamicRedisConnectionFactory implements RedisConnectionFactory,ApplicationContextAware{

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRedisConnectionFactory.class);
  private AtomicReference<RedisConnectionFactory> redisConnectionFactory;
  private ApplicationContext applicationContext;
  
//  @Autowired
//  private RedisAccessor accessor;
  

  public DynamicRedisConnectionFactory(
      RedisConnectionFactory redisConnectionFactory) {
    super();
    this.redisConnectionFactory =new AtomicReference<RedisConnectionFactory>(redisConnectionFactory);
  }

  public RedisConnectionFactory getAndSet(RedisConnectionFactory connectionFactory) {
//    Map<String, RedisTemplate> beansOfType = applicationContext.getBeansOfType(RedisTemplate.class);
//    for (Map.Entry<String, RedisTemplate> entry : beansOfType.entrySet()) {
//      LOGGER.info("RedisTemplate替换连接工厂BEANNAME[{}]",entry.getKey());
//      entry.getValue().setConnectionFactory(connectionFactory);
//    }
    return redisConnectionFactory.getAndSet(connectionFactory);
  }
  
  @Override
  public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
    return redisConnectionFactory.get().translateExceptionIfPossible(ex);
  }

  @Override
  public RedisConnection getConnection() {
    return redisConnectionFactory.get().getConnection();
  }

  @Override
  public RedisClusterConnection getClusterConnection() {
    
    return redisConnectionFactory.get().getClusterConnection();
  }

  @Override
  public boolean getConvertPipelineAndTxResults() {
    return redisConnectionFactory.get().getConvertPipelineAndTxResults();
  }

  @Override
  public RedisSentinelConnection getSentinelConnection() {
    return redisConnectionFactory.get().getSentinelConnection();
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext)
      throws BeansException {
    
    this.applicationContext = applicationContext;    
  }
  
  
}

