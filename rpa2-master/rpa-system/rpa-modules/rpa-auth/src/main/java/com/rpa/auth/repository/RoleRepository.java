package com.rpa.auth.repository;

import com.rpa.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Role findByRoleName(String roleName);
}
