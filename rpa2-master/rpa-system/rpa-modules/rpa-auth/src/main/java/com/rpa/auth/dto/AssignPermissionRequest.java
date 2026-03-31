package com.rpa.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 为角色批量分配权限请求 DTO。
 */
public class AssignPermissionRequest {

    @NotEmpty(message = "权限 ID 列表不能为空")
    private List<Long> permissionIds;

    public List<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }

    // 兼容后端旧的 permIds 调用
    public List<Long> getPermIds() { return permissionIds; }
    public void setPermIds(List<Long> permIds) { this.permissionIds = permIds; }
}
