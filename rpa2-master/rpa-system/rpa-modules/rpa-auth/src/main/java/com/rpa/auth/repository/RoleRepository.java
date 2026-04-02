package com.rpa.auth.repository;

import com.rpa.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Role findByRoleName(String roleName);
    
    @Query("SELECT r FROM Role r WHERE r.isDeleted = 0")
    List<Role> findByIsDeletedOrderByCreateTimeDesc();
    
    @Query("SELECT r FROM Role r WHERE r.isDeleted = 0")
    Page<Role> findByIsDeleted(Pageable pageable);
    
    @Query("SELECT r FROM Role r WHERE r.isDeleted = 0 AND r.id = ?1")
    Role findByIdAndIsDeleted(Long id);
}
