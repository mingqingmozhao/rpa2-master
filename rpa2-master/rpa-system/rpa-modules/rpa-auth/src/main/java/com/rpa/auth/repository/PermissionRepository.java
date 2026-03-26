package com.rpa.auth.repository;

import com.rpa.auth.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据角色ID查询权限列表
     */
    @Query("SELECT p FROM Permission p INNER JOIN RolePermission rp ON p.permId = rp.permId " +
           "WHERE rp.roleId = :roleId AND p.isDeleted = 0")
    List<Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询所有权限
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "INNER JOIN RolePermission rp ON p.permId = rp.permId " +
           "INNER JOIN UserRole ur ON rp.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND p.isDeleted = 0")
    List<Permission> findPermissionsByUserId(@Param("userId") Long userId);
}
