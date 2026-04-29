package com.rpa.task.repository;

import com.rpa.task.model.TaskInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskInfoRepository extends JpaRepository<TaskInfo, Long> {
    
    boolean existsByTaskCode(String taskCode);
    
    @Query("SELECT t FROM TaskInfo t WHERE t.isDeleted = 0 " +
           "AND (:keyword IS NULL OR t.taskName LIKE %:keyword% OR t.taskCode LIKE %:keyword%) " +
           "AND (:processId IS NULL OR t.processId = :processId) " +
           "AND (:robotId IS NULL OR t.robotId = :robotId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:startDate IS NULL OR t.createTime >= :startDate) " +
           "AND (:endDate IS NULL OR t.createTime <= :endDate)")
    Page<TaskInfo> findAll(@Param("keyword") String keyword,
                           @Param("processId") Long processId,
                           @Param("robotId") Long robotId,
                           @Param("status") Integer status,
                           @Param("startDate") java.time.LocalDateTime startDate,
                           @Param("endDate") java.time.LocalDateTime endDate,
                           Pageable pageable);
    
    /**
     * 获取第一个待执行的任务（按优先级和时间排序）
     */
    @Query("SELECT t FROM TaskInfo t WHERE t.status = :status AND t.isDeleted = 0 ORDER BY t.priority DESC, t.createTime ASC")
    TaskInfo findFirstByStatusAndEnabledTrueOrderByPriorityDescCreateTimeAsc(Integer status);
    
    /**
     * 按执行时间范围查询任务ID列表（去重）
     * 查找在指定时间范围内有执行记录的任务
     */
    @Query("SELECT DISTINCT t.id FROM TaskInfo t JOIN TaskExecutionQueue q ON t.id = q.taskId " +
           "WHERE q.startTime >= :startTime AND q.startTime <= :endTime AND t.isDeleted = 0")
    List<Long> findTaskIdsByExecutionTimeBetween(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);
    
    /**
     * 批量删除任务（物理删除）
     */
    void deleteByIdIn(List<Long> ids);
    
    /**
     * 根据ID列表分页查询任务（用于按执行时间筛选）
     */
    @Query("SELECT t FROM TaskInfo t WHERE t.id IN :ids " +
           "AND (:keyword IS NULL OR t.taskName LIKE %:keyword% OR t.taskCode LIKE %:keyword%) " +
           "AND (:processId IS NULL OR t.processId = :processId) " +
           "AND (:robotId IS NULL OR t.robotId = :robotId) " +
           "AND (:status IS NULL OR t.status = :status)")
    Page<TaskInfo> findByIdIn(@Param("ids") List<Long> ids,
                             @Param("keyword") String keyword,
                             @Param("processId") Long processId,
                             @Param("robotId") Long robotId,
                             @Param("status") Integer status,
                             Pageable pageable);
}
