package com.rpa.auth.dto;

import lombok.Data;
import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserInfo userInfo;
    private List<String> roles;
    private List<String> permissions;

    @Data
    public static class UserInfo {
        private Long userId;
        private String username;
        private String nickname;
        private String email;
        private String phone;
        private String avatar;
        private Integer status;
    }
}
