package com.rpa.auth.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseTableFixer {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/financial_data_collect?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "123456";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("数据库连接成功！");
            
            try (Statement stmt = conn.createStatement()) {
                // 添加 category 列
                try {
                    stmt.executeUpdate("ALTER TABLE task_info ADD COLUMN category VARCHAR(20) COMMENT '分类'");
                    System.out.println("✓ 添加 category 列成功");
                } catch (Exception e) {
                    if (e.getMessage().contains("Duplicate column name")) {
                        System.out.println("✓ category 列已存在");
                    } else {
                        System.out.println("✗ 添加 category 列失败：" + e.getMessage());
                    }
                }
                
                // 添加 priority 列
                try {
                    stmt.executeUpdate("ALTER TABLE task_info ADD COLUMN priority INT DEFAULT 5 COMMENT '优先级'");
                    System.out.println("✓ 添加 priority 列成功");
                } catch (Exception e) {
                    if (e.getMessage().contains("Duplicate column name")) {
                        System.out.println("✓ priority 列已存在");
                    } else {
                        System.out.println("✗ 添加 priority 列失败：" + e.getMessage());
                    }
                }
                
                // 添加 remark 列
                try {
                    stmt.executeUpdate("ALTER TABLE task_info ADD COLUMN remark VARCHAR(255) COMMENT '备注'");
                    System.out.println("✓ 添加 remark 列成功");
                } catch (Exception e) {
                    if (e.getMessage().contains("Duplicate column name")) {
                        System.out.println("✓ remark 列已存在");
                    } else {
                        System.out.println("✗ 添加 remark 列失败：" + e.getMessage());
                    }
                }
                
                // 添加 enterprise_name 列
                try {
                    stmt.executeUpdate("ALTER TABLE task_info ADD COLUMN enterprise_name VARCHAR(100) COMMENT '企业名称'");
                    System.out.println("✓ 添加 enterprise_name 列成功");
                } catch (Exception e) {
                    if (e.getMessage().contains("Duplicate column name")) {
                        System.out.println("✓ enterprise_name 列已存在");
                    } else {
                        System.out.println("✗ 添加 enterprise_name 列失败：" + e.getMessage());
                    }
                }
                
                // 添加 tax_no 列
                try {
                    stmt.executeUpdate("ALTER TABLE task_info ADD COLUMN tax_no VARCHAR(20) COMMENT '税号'");
                    System.out.println("✓ 添加 tax_no 列成功");
                } catch (Exception e) {
                    if (e.getMessage().contains("Duplicate column name")) {
                        System.out.println("✓ tax_no 列已存在");
                    } else {
                        System.out.println("✗ 添加 tax_no 列失败：" + e.getMessage());
                    }
                }
                
System.out.println("\n表结构修复完成！");
            }
            
        } catch (Exception e) {
            System.err.println("错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
