package com.rpa.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {
    "com.rpa.application",
    "com.rpa.auth",
    "com.rpa.task",
    "com.rpa.process"
})
@EnableJpaRepositories(basePackages = {
    "com.rpa.auth.repository",
    "com.rpa.task.repository",
    "com.rpa.process.repository"
})
@EntityScan(basePackages = {
    "com.rpa.auth.model",
    "com.rpa.task.model",
    "com.rpa.process.model"
})
public class RpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpaApplication.class, args);
    }
}
