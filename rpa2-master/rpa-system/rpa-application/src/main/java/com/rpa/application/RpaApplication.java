package com.rpa.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.rpa.application",
    "com.rpa.auth",
    "com.rpa.task",
    "com.rpa.process",
    "com.rpa.robot",
    "com.rpa.client",
    "com.rpa.application.listener"
})
@EnableJpaRepositories(basePackages = {
    "com.rpa.auth.repository",
    "com.rpa.task.repository",
    "com.rpa.process.repository",
    "com.rpa.robot.repository",
    "com.rpa.client.repository"
})
@EntityScan(basePackages = {
    "com.rpa.auth.model",
    "com.rpa.task.model",
    "com.rpa.process.model",
    "com.rpa.robot.model",
    "com.rpa.client.model"
})
@EnableScheduling // 启用定时任务调度
public class RpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpaApplication.class, args);
    }
}
