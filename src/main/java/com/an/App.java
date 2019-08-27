package com.an;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.an.repository.UserRepository;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;

@EnableApolloConfig
@SpringBootApplication
public class App  implements CommandLineRunner {

  @Autowired
  private UserRepository repository;
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    Executors.newSingleThreadExecutor().submit(()->{
      while(true) {
        System.err.println(repository.findById(1).get().getName());
        TimeUnit.SECONDS.sleep(1);
      }
    });
  }
}

