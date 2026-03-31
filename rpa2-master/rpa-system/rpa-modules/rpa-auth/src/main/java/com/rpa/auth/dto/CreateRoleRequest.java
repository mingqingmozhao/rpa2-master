package com.rpa.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建角色请求 DTO。
 */
public class CreateRoleRequest {

    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称最多30字符")
    private String roleName;

    @Size(max = 100, message = "角色描述最多100字符")
    private String remark;

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
