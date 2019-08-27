package com.an.config;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class DynamicDataSource implements DataSource {

  private AtomicReference<DataSource> dataSource;
  
  public DynamicDataSource(DataSource dataSource) {
    this.dataSource = new AtomicReference<DataSource>(dataSource);
  }
  
  /**
   * set the new data source and return the previous one
   */
  public DataSource setDataSource(DataSource newDataSource){
    return dataSource.getAndSet(newDataSource);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    
    return dataSource.get().getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    
    dataSource.get().setLogWriter(out);    
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    
     dataSource.get().setLoginTimeout(seconds);
    
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return dataSource.get().getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    
    return dataSource.get().getParentLogger();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    
    return dataSource.get().unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    
    return dataSource.get().isWrapperFor(iface);
  }

  @Override
  public Connection getConnection() throws SQLException {
    
    return dataSource.get().getConnection();
  }

  @Override
  public Connection getConnection(String username, String password)
      throws SQLException {
    return dataSource.get().getConnection(username,password);
  }

}

