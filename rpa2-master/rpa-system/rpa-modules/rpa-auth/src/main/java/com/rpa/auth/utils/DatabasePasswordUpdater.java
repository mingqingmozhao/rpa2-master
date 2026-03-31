package com.rpa.auth.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DatabasePasswordUpdater {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/financial_data_collect?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "123456";
        
        // BCrypt hash for "admin123"
        String bcryptHash = "$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO";
        
        String[] usernames = {"admin", "operator01", "operator02", "operator03", "viewer01", "viewer02", "viewer03", "viewer04"};
        
        String updateSQL = "UPDATE sys_user SET password = ? WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("数据库连接成功！");
            
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                for (String username : usernames) {
                    pstmt.setString(1, bcryptHash);
                    pstmt.setString(2, username);
                    int rows = pstmt.executeUpdate();
                    System.out.println("更新用户 " + username + ": " + rows + " 行受影响");
                }
            }
            
            System.out.println("\n所有用户密码已更新为：admin123");
        } catch (Exception e) {
            System.err.println("错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
