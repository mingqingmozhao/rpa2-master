package com.rpa.auth.dto;

import com.rpa.auth.model.Permission;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限树形结构返回 DTO（用于前端菜单树渲染）。
 * 支持 children 嵌套，实现树形扁平转树结构。
 */
public class PermissionTreeResponse {

    private Long id;
    private String permName;
    private String permKey;
    private Integer permType;
    private Long parentId;
    private LocalDateTime createTime;
    private List<PermissionTreeResponse> children = new ArrayList<>();

    public PermissionTreeResponse() {}

    public PermissionTreeResponse(Permission p) {
        this.id = p.getId();
        this.permName = p.getPermName();
        this.permKey = p.getPermKey();
        this.permType = p.getPermType();
        this.parentId = p.getParentId();
        this.createTime = p.getCreateTime();
    }

    // 兼容前端 el-tree 使用 name 字段
    public String getName() { return permName; }

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

    public List<PermissionTreeResponse> getChildren() { return children; }
    public void setChildren(List<PermissionTreeResponse> children) { this.children = children; }

    /** 将扁平权限列表转为一棵树 */
    public static List<PermissionTreeResponse> buildTree(List<Permission> flat) {
        List<PermissionTreeResponse> roots = new ArrayList<>();
        for (Permission p : flat) {
            // parent_id 为 null 或 0 表示根节点
            if (p.getParentId() == null || p.getParentId() == 0L) {
                roots.add(toNode(p));
            }
        }
        // 递归填充 children
        fillChildren(flat, roots);
        return roots;
    }

    private static PermissionTreeResponse toNode(Permission p) {
        PermissionTreeResponse node = new PermissionTreeResponse(p);
        return node;
    }

    private static void fillChildren(List<Permission> flat, List<PermissionTreeResponse> nodes) {
        for (PermissionTreeResponse node : nodes) {
            List<PermissionTreeResponse> children = new ArrayList<>();
            for (Permission p : flat) {
                if (p.getParentId() != null && p.getParentId().equals(node.getId())) {
                    PermissionTreeResponse child = toNode(p);
                    fillChildren(flat, List.of(child));
                    children.add(child);
                }
            }
            node.setChildren(children);
        }
    }
}
