package com.rpa.auth.repository;

import com.rpa.auth.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByPermId(Long permId);

    void deleteByRoleId(Long roleId);

    void deleteByPermId(Long permId);

    void deleteByRoleIdAndPermId(Long roleId, Long permId);

    boolean existsByRoleIdAndPermId(Long roleId, Long permId);

    long countByRoleId(Long roleId);
}
