package com.rpa.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "姓名不能为空")
    private String realName;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String phone;
    
    @NotNull(message = "角色不能为空")
    private Long roleId;
    
    private String remark;
}
