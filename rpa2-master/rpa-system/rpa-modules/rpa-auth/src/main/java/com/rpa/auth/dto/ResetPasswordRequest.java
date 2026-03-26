package com.rpa.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "新密码不能为空")
    private String password;
}
