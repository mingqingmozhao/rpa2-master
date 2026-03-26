package com.rpa.auth.repository;

import com.rpa.auth.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * 根据用户 ID 查询用户角色关联列表
     */
    List<UserRole> findByUserId(Long userId);
    
    /**
     * 根据用户 ID 删除所有角色关联
     */
    void deleteByUserId(Long userId);
}
