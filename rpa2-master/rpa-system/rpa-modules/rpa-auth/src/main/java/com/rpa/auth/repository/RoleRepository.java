package com.rpa.auth.repository;

import com.rpa.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据用户ID查询角色列表
     */
    @Query("SELECT r FROM Role r INNER JOIN UserRole ur ON r.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND r.status = 1 AND r.isDeleted = 0")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
}
