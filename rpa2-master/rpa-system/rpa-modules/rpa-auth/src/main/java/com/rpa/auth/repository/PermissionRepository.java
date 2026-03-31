package com.rpa.auth.repository;

import com.rpa.auth.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermKey(String permKey);

    boolean existsByPermKey(String permKey);

    List<Permission> findByParentId(Long parentId);

    List<Permission> findByParentIdIsNull();

    List<Permission> findByPermType(Integer permType);

    List<Permission> findByParentIdAndIsDeleted(Long parentId, Integer isDeleted);

    @Query("SELECT p FROM Permission p WHERE p.isDeleted = 0 " +
           "AND (:permName IS NULL OR p.permName LIKE %:permName%) " +
           "AND (:permKey IS NULL OR p.permKey LIKE %:permKey%) " +
           "AND (:permType IS NULL OR p.permType = :permType)")
    List<Permission> searchPermissions(
            @Param("permName") String permName,
            @Param("permKey") String permKey,
            @Param("permType") Integer permType);
}
