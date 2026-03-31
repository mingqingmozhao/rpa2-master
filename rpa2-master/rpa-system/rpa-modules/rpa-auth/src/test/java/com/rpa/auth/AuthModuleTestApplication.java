package com.rpa.auth;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * rpa-auth 模块集成测试用启动类（供 {@code @SpringBootTest(classes=...)} 使用）。
 */
@SpringBootApplication(scanBasePackages = "com.rpa.auth")
@EnableJpaRepositories(basePackages = "com.rpa.auth.repository")
@EntityScan(basePackages = "com.rpa.auth.model")
public class AuthModuleTestApplication {
}
