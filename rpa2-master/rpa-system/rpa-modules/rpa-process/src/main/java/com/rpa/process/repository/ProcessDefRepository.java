package com.rpa.process.repository;

import com.rpa.process.model.ProcessDef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessDefRepository extends JpaRepository<ProcessDef, Long> {
    
    Optional<ProcessDef> findByProcessCode(String processCode);
    
    boolean existsByProcessCode(String processCode);
    
    boolean existsByProcessCodeAndIdNot(String processCode, Long id);
    
    @Query("SELECT p FROM ProcessDef p WHERE (p.isDeleted = 0 OR p.isDeleted IS NULL) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "p.processCode LIKE %:keyword% OR p.processName LIKE %:keyword% OR p.category LIKE %:keyword%) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<ProcessDef> searchProcesses(@Param("keyword") String keyword, 
                                     @Param("status") Integer status, 
                                     Pageable pageable);
}
