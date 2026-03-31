package com.rpa.auth.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DatabaseRoleFixer {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/financial_data_collect?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "123456";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("数据库连接成功！");
            
            // 先查询用户 ID 和对应的角色
            String selectSQL = "SELECT u.id, u.username, " +
                              "CASE WHEN u.username = 'admin' THEN 1 " +
                              "     WHEN u.username LIKE 'operator%' THEN 2 " +
                              "     ELSE 3 END as role_id " +
                              "FROM sys_user u " +
                              "WHERE u.username IN ('admin', 'operator01', 'operator02', 'operator03', 'viewer01', 'viewer02', 'viewer03', 'viewer04')";
            
            try (PreparedStatement pstmt = conn.prepareStatement(selectSQL);
                 java.sql.ResultSet rs = pstmt.executeQuery()) {
                
                String insertSQL = "INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    
                    int count = 0;
                    while (rs.next()) {
                        long userId = rs.getLong("id");
                        long roleId = rs.getLong("role_id");
                        
                        insertStmt.setLong(1, userId);
                        insertStmt.setLong(2, roleId);
                        int rows = insertStmt.executeUpdate();
                        
                        System.out.println("用户 ID " + userId + " -> 角色 ID " + roleId + ": " + 
                                         (rows > 0 ? "已插入" : "已存在"));
                        count++;
                    }
                    
                    System.out.println("\n共处理 " + count + " 个用户的角色分配");
                }
            }
            
        } catch (Exception e) {
            System.err.println("错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
