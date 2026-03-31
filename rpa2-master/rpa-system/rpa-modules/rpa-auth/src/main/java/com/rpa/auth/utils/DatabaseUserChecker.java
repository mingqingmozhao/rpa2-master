package com.rpa.auth.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseUserChecker {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/financial_data_collect?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "123456";
        
        String querySQL = "SELECT id, username, real_name, status FROM sys_user WHERE username IN ('admin', 'operator01', 'operator02')";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("数据库连接成功！");
            
            try (PreparedStatement pstmt = conn.prepareStatement(querySQL);
                 ResultSet rs = pstmt.executeQuery()) {
                
                System.out.println("\n用户数据：");
                System.out.println("ID\t用户名\t\t真实姓名\t状态");
                System.out.println("----------------------------------------");
                
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String realName = rs.getString("real_name");
                    int status = rs.getInt("status");
                    
                    System.out.println(id + "\t" + username + "\t\t" + realName + "\t\t" + (status == 1 ? "正常" : "禁用"));
                }
            }
            
            // 检查角色分配
            String roleQuery = "SELECT ur.user_id, ur.role_id, r.role_name " +
                              "FROM sys_user_role ur " +
                              "JOIN sys_role r ON ur.role_id = r.id " +
                              "WHERE ur.user_id IN (SELECT id FROM sys_user WHERE username IN ('admin', 'operator01', 'operator02'))";
            
            try (PreparedStatement pstmt = conn.prepareStatement(roleQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                
                System.out.println("\n用户角色分配：");
                System.out.println("用户 ID\t角色 ID\t角色名称");
                System.out.println("----------------------------------------");
                
                while (rs.next()) {
                    long userId = rs.getLong("user_id");
                    long roleId = rs.getLong("role_id");
                    String roleName = rs.getString("role_name");
                    
                    System.out.println(userId + "\t" + roleId + "\t" + roleName);
                }
            }
            
        } catch (Exception e) {
            System.err.println("错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
