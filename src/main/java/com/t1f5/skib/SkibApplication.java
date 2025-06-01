package com.t1f5.skib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SkibApplication {

  public static void main(String[] args) {
    SpringApplication.run(SkibApplication.class, args);
  }
}
