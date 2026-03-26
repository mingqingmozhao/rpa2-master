package com.rpa.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.rpa")
@EnableJpaRepositories(basePackages = "com.rpa")
@EntityScan(basePackages = "com.rpa")
public class RpaApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpaApplication.class, args);
    }
}
