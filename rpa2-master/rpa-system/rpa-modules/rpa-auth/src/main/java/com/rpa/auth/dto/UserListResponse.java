package com.rpa.auth.dto;

import lombok.Data;

@Data
public class UserListResponse {
    private Long userId;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String nickname;
    private Integer status;
    private String roleName;
    private String createTime;
    private String remark;
}
