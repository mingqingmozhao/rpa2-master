package com.rpa.task.repository;

import com.rpa.task.model.TaskInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskInfoRepository extends JpaRepository<TaskInfo, Long> {
    
    boolean existsByTaskCode(String taskCode);
    
    @Query("SELECT t FROM TaskInfo t WHERE t.isDeleted = 0 " +
           "AND (:keyword IS NULL OR t.taskName LIKE %:keyword% OR t.taskCode LIKE %:keyword%) " +
           "AND (:processId IS NULL OR t.processId = :processId) " +
           "AND (:status IS NULL OR t.status = :status)")
    Page<TaskInfo> findAll(@Param("keyword") String keyword,
                           @Param("processId") Long processId,
                           @Param("status") Integer status,
                           Pageable pageable);
}
