package com.rpa.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新角色请求 DTO。
 */
public class UpdateRoleRequest {

    private String roleCode;

    @Size(max = 30, message = "角色名称最多30字符")
    private String roleName;

    @Size(max = 100, message = "角色描述最多100字符")
    private String remark;

    private Integer status;  // 1=启用 0=禁用

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
