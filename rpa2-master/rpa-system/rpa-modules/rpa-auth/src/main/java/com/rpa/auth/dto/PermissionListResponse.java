package com.rpa.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rpa.auth.model.Permission;

import java.time.LocalDateTime;

/**
 * 权限列表返回 DTO。
 */
public class PermissionListResponse {

    private Long id;
    private String permName;
    private String permKey;
    private Integer permType;      // 1=菜单 2=按钮
    private Long parentId;
    private LocalDateTime createTime;

    public PermissionListResponse() {}

    public PermissionListResponse(Permission p) {
        this.id = p.getId();
        this.permName = p.getPermName();
        this.permKey = p.getPermKey();
        this.permType = p.getPermType();
        this.parentId = p.getParentId();
        this.createTime = p.getCreateTime();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }

    public String getPermKey() { return permKey; }
    public void setPermKey(String permKey) { this.permKey = permKey; }

    public Integer getPermType() { return permType; }
    public void setPermType(Integer permType) { this.permType = permType; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
