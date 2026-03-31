package com.rpa.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新个人资料请求 DTO。
 */
public class UpdateUserInfoRequest {

    @Size(max = 30, message = "姓名最多 30 字符")
    private String realName;

    @Size(max = 100, message = "邮箱最多 100 字符")
    @Pattern(regexp = "^$|^[\\w.-]+@[\\w.-]+\\.\\w+$", message = "邮箱格式不正确")
    private String email;

    @Size(max = 11, message = "手机号最多 11 位")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
