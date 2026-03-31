package com.rpa.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新权限请求 DTO（只允许修改 permName / parentId / status）。
 * permKey 一旦创建不可更改。
 */
public class UpdatePermissionRequest {

    @Size(max = 50, message = "权限名称最多50字符")
    private String permName;

    private Long parentId;

    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
