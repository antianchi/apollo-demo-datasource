package com.an.util;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.an.config.DynamicDataSource;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

@Component
public class DataSourceRefresher implements ApplicationContextAware {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(DataSourceRefresher.class);
  private ScheduledExecutorService scheduledExecutorService = Executors
      .newSingleThreadScheduledExecutor();

  @Autowired
  private DynamicDataSource dynamicDataSource;

  @Autowired
  private DataSourceManager dataSourceManager;

  @Autowired
  private ApplicationContext applicationContext;

  @ApolloConfigChangeListener(interestedKeyPrefixes = {"spring.datasource."})
  public void onChange(ConfigChangeEvent changeEvent) {
    refreshDataSource(changeEvent.changedKeys());
  }

  private void refreshDataSource(Set<String> changedKeys) {
    try {
      LOGGER.info("刷新数据源-------------》，变更KEYS[{}]", changedKeys);
      /**
       * rebind configuration beans, e.g. DataSourceProperties
       * 
       * @see org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder#onApplicationEvent
       */
      this.applicationContext
          .publishEvent(new EnvironmentChangeEvent(changedKeys));

      DataSource newDataSource = dataSourceManager
          .createAndTestHikariDataSource();
      DataSource olDataSource = dynamicDataSource.setDataSource(newDataSource);
      asyncTerminate(olDataSource);

      LOGGER.info("Finished refreshing data source");

    } catch (SQLException e) {
      e.printStackTrace();
      LOGGER.error("Refreshing data source failed", e);
    }

  }

  private void asyncTerminate(DataSource olDataSource) {

    DataSourceTerminationTask task = new DataSourceTerminationTask(olDataSource,
        scheduledExecutorService);

    // start now
    scheduledExecutorService.schedule(task, 0, TimeUnit.MILLISECONDS);

  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext)
      throws BeansException {

    this.applicationContext = applicationContext;
  }

}
