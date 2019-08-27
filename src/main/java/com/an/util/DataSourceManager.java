package com.an.util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

@Component
public class DataSourceManager {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DataSourceManager.class);

  @Autowired
  private CustomizedConfigurationPropertiesBinder binder;

  @Autowired
  private DataSourceProperties dataSourceProperties;

  public HikariDataSource createHikariDataSource() {
    HikariDataSource hikariDataSource = dataSourceProperties
        .initializeDataSourceBuilder().type(HikariDataSource.class).build();
    if (StringUtils.hasText(dataSourceProperties.getName())) {
      hikariDataSource.setPoolName(dataSourceProperties.getName());
    }
    Bindable<?> target = Bindable.of(HikariDataSource.class)
        .withExistingValue(hikariDataSource);
    this.binder.bind("spring.datasource.hikari", target);
    return hikariDataSource;
  }

  public HikariDataSource createAndTestHikariDataSource() throws SQLException {
    HikariDataSource newDataSource = createHikariDataSource();
    try {
      testConnection(newDataSource);
    } catch (SQLException ex) {
      LOGGER.error("Testing connection for data source failed: {}--{}",
          newDataSource.getJdbcUrl(), ex);
      newDataSource.close();
      throw ex;
    }

    return newDataSource;
  }

  private void testConnection(DataSource dataSource) throws SQLException {
    // borrow a connection
    Connection connection = dataSource.getConnection();
    // return the connection
    connection.close();
  }
}
