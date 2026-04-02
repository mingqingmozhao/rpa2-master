package com.rpa.auth.repository;

import com.rpa.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByPhone(String phone);
    
    boolean existsByUsername(String username);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT u FROM User u WHERE u.isDeleted = 0 " +
           "AND (:username IS NULL OR u.username LIKE %:username%) " +
           "AND (:realName IS NULL OR u.realName LIKE %:realName%) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:roleId IS NULL OR EXISTS (SELECT 1 FROM UserRole ur WHERE ur.userId = u.id AND ur.roleId = :roleId))")
    Page<User> findAll(@Param("username") String username,
                       @Param("realName") String realName,
                       @Param("roleId") Long roleId,
                       @Param("status") Integer status,
                       Pageable pageable);
}
