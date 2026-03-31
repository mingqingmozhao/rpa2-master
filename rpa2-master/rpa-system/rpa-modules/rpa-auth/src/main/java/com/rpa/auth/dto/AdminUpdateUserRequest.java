package com.rpa.auth.dto;

public class AdminUpdateUserRequest {
    
    private String realName;
    private String email;
    private String phone;
    private Integer status;
    private Long roleId;
    private String remark;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
