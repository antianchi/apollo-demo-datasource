package com.an;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.an.config.DynamicDataSource;
import com.an.util.DataSourceManager;

//@Configuration
public class RefreshableDataSourceConfiguration {

//  @Bean
  public DynamicDataSource dataSource(DataSourceManager dataSourceManager) {
    DataSource dataSource = dataSourceManager.createHikariDataSource();
    return new DynamicDataSource(dataSource);
    
  }
}

