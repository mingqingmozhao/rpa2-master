package com.rpa.auth.repository;

import com.rpa.auth.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    /**
     * 根据角色ID查询角色权限关联列表
     */
    List<RolePermission> findByRoleId(Long roleId);
}
