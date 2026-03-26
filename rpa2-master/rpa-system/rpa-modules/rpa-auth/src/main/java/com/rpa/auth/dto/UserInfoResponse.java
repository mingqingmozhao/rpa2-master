package com.rpa.auth.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private List<String> roles;
    private List<String> permissions;
}
