package com.an.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class RedisConnectionTerminationTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RedisConnectionTerminationTask.class);
  private static final int RETRY_DELAY_IN_MILLISECONDS = 5000;

  private final RedisConnectionFactory redisConnectionFactory;
  private final ScheduledExecutorService scheduledExecutorService;

  public RedisConnectionTerminationTask(
      RedisConnectionFactory redisConnectionFactory,
      ScheduledExecutorService scheduledExecutorService) {
    this.redisConnectionFactory = redisConnectionFactory;
    this.scheduledExecutorService = scheduledExecutorService;
  }

  @Override
  public void run() {
    if (terminate(redisConnectionFactory)) {
      LOGGER.info("Redis连接 [{}]成功终止!", redisConnectionFactory);
    } else {
      scheduledExecutorService.schedule(this, RETRY_DELAY_IN_MILLISECONDS,
          TimeUnit.MILLISECONDS);
    }
  }

  private boolean terminate(RedisConnectionFactory redisConnectionFactory) {
    try {
      LOGGER.info("尝试终止REDIS连接： [{}]", redisConnectionFactory);
      if (redisConnectionFactory instanceof LettuceConnectionFactory) {
        return terminateRedisConnectionFactory(
            (LettuceConnectionFactory) redisConnectionFactory);
      }

      LOGGER.error("不支持的REDIS连接工厂: [{}]", redisConnectionFactory);

      return true;
    } catch (Throwable ex) {
      LOGGER.warn(
          "Terminating REDIS 连接  {} failed, will retry in {} ms, error message: {}",
          redisConnectionFactory, RETRY_DELAY_IN_MILLISECONDS, ex.getMessage());
      return false;
    } finally {
    }
  }

  private boolean terminateRedisConnectionFactory(
      LettuceConnectionFactory redisConnectionFactory) {
    // redisConnectionFactory.destroy();
    LOGGER.info("终止连接工厂，目前还没找到好的方法！！");
    return true;

  }

}
