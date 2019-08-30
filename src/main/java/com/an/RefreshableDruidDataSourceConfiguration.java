package com.an;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.an.config.DynamicDataSource;
import com.an.util.DruidDataSourceManager;

@Configuration
public class RefreshableDruidDataSourceConfiguration {

  @Bean
  public DynamicDataSource dataSource(DruidDataSourceManager dataSourceManager) {
    DataSource dataSource = dataSourceManager.createDruidDataSource();
    System.out.println("数据源的类型：" + dataSource.getClass().getName());
    return new DynamicDataSource(dataSource);
    
  }
}

