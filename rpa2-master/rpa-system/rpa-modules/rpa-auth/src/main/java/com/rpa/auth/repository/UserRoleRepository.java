package com.rpa.auth.repository;

import com.rpa.auth.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    List<UserRole> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    @Query(value = "SELECT * FROM sys_user_role WHERE role_id = :roleId", nativeQuery = true)
    List<UserRole> findByRoleId(@Param("roleId") Long roleId);
}
