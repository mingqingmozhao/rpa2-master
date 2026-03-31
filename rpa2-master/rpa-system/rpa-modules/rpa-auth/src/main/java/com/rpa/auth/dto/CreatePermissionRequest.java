package com.rpa.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建权限请求 DTO。
 *
 * @param permName  权限名称（如"用户管理"、"新增按钮"）
 * @param permKey   权限标识，唯一（如"system:user:create"，前端用于鉴权 key）
 * @param permType  权限类型：1=菜单，2=按钮
 * @param parentId  父权限 ID（顶级为 null）
 */
public class CreatePermissionRequest {

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称最多50字符")
    private String permName;

    @NotBlank(message = "权限标识不能为空")
    @Size(max = 100, message = "权限标识最多100字符")
    private String permKey;

    @NotNull(message = "权限类型不能为空")
    private Integer permType;

    private Long parentId;

    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }

    public String getPermKey() { return permKey; }
    public void setPermKey(String permKey) { this.permKey = permKey; }

    public Integer getPermType() { return permType; }
    public void setPermType(Integer permType) { this.permType = permType; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
