package com.an.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;

@Component
public class DruidDataSourceManager {

  @Autowired
  private CustomizedConfigurationPropertiesBinder binder;
  
  @Autowired
  private DataSourceProperties dataSourceProperties;
  
  public DruidDataSource createDruidDataSource() {
  
    DruidDataSource druidDataSource = dataSourceProperties.initializeDataSourceBuilder().type(DruidDataSource.class).build();
    Bindable<?> target = Bindable.of(DruidDataSource.class)
        .withExistingValue(druidDataSource);
    this.binder.bind("spring.datasource.druid", target);
    return druidDataSource;
  }
  
}

