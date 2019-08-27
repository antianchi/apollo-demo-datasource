package com.an.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

public class DataSourceTerminationTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DataSourceTerminationTask.class);
  private static final int MAX_RETRY_TIMES = 10;
  private static final int RETRY_DELAY_IN_MILLISECONDS = 5000;

  private final DataSource dataSourceToTerminate;
  private final ScheduledExecutorService scheduledExecutorService;

  private volatile int retryTimes;

  public DataSourceTerminationTask(DataSource dataSourceToTerminate,
      ScheduledExecutorService scheduledExecutorService) {
    this.dataSourceToTerminate = dataSourceToTerminate;
    this.scheduledExecutorService = scheduledExecutorService;
    this.retryTimes = 0;
  }

  @Override
  public void run() {
    if (terminate(dataSourceToTerminate)) {
      LOGGER.info("Data source {} 成功终止!", dataSourceToTerminate);
    } else {
      scheduledExecutorService.schedule(this, RETRY_DELAY_IN_MILLISECONDS,
          TimeUnit.MILLISECONDS);
    }
  }

  private boolean terminate(DataSource dataSource) {
    try {
      LOGGER.info("尝试终止数据源： [{}]", dataSource);
      if (dataSource instanceof HikariDataSource) {
        return terminateHikariDataSource((HikariDataSource) dataSource);
      }

      LOGGER.error("不支持的数据源: [{}]", dataSource);

      return true;
    } catch (Throwable ex) {
      LOGGER.warn(
          "Terminating data source {} failed, will retry in {} ms, error message: {}",
          dataSource, RETRY_DELAY_IN_MILLISECONDS, ex.getMessage());
      return false;
    } finally {
      retryTimes++;
    }
  }

  private boolean terminateHikariDataSource(HikariDataSource dataSource) {
    HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
    // 丢弃空闲连接
    poolMXBean.softEvictConnections();

    if (poolMXBean.getActiveConnections() > 0 && retryTimes < MAX_RETRY_TIMES) {
      LOGGER.warn(
          "Data source {} still has {} active connections, will retry in {} ms.",
          dataSource, poolMXBean.getActiveConnections(),
          RETRY_DELAY_IN_MILLISECONDS);
      return false;
    }

    if (poolMXBean.getActiveConnections() > 0) {
      LOGGER.warn(
          "Retry times({}) >= {}, force closing data source {}, with {} active connections!",
          retryTimes, MAX_RETRY_TIMES, dataSource,
          poolMXBean.getActiveConnections());
    }

    dataSource.close();
    return true;
  }

}
