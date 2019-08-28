package com.an.config;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.core.RedisTemplate;

public class DynamicRedisConnectionFactory implements RedisConnectionFactory,ApplicationContextAware{

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
    RedisTemplate bean2 = (RedisTemplate) applicationContext.getBean("redisTemplate");
    RedisTemplate bean3 = (RedisTemplate) applicationContext.getBean("stringRedisTemplate");
    bean2.setConnectionFactory(connectionFactory);
    bean3.setConnectionFactory(connectionFactory);
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

