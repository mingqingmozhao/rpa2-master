package com.rpa.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class RoleDetailResponse {
    private Long id;
    private String roleName;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    
    @JsonProperty("permissions")
    private List<PermissionInfo> permissions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public List<PermissionInfo> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionInfo> permissions) { this.permissions = permissions; }
}
