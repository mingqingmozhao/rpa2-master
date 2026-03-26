package com.rpa.auth.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;
    
    private String email;
    
    private String phone;
    
    private String avatar;
}
