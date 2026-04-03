package com.rpa.robot.repository;

import com.rpa.robot.model.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long> {
    
    /**
     * 根据机器人编码查询
     */
    Optional<Robot> findByRobotCode(String robotCode);
    
    /**
     * 检查机器人编码是否存在
     */
    boolean existsByRobotCode(String robotCode);
    
    /**
     * 根据状态查询机器人列表
     */
    List<Robot> findByStatus(String status);
    
    /**
     * 查询所有可用的机器人（在线或忙碌）
     */
    @Query("SELECT r FROM Robot r WHERE r.status IN ('ONLINE', 'BUSY') AND r.isDeleted = 0")
    List<Robot> findAvailableRobots();
    
    /**
     * 查询在线机器人数量
     */
    @Query("SELECT COUNT(r) FROM Robot r WHERE r.status = 'ONLINE' AND r.isDeleted = 0")
    Long countOnlineRobots();
    
    /**
     * 查询离线机器人数量
     */
    @Query("SELECT COUNT(r) FROM Robot r WHERE r.status = 'OFFLINE' AND r.isDeleted = 0")
    Long countOfflineRobots();
    
    /**
     * 查询忙碌机器人数量
     */
    @Query("SELECT COUNT(r) FROM Robot r WHERE r.status = 'BUSY' AND r.isDeleted = 0")
    Long countBusyRobots();
    
    /**
     * 查询故障机器人数量
     */
    @Query("SELECT COUNT(r) FROM Robot r WHERE r.status = 'FAULT' AND r.isDeleted = 0")
    Long countFaultRobots();
    
    /**
     * 搜索机器人（支持编码、名称、部门模糊查询）
     */
    @Query("SELECT r FROM Robot r WHERE r.isDeleted = 0 AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "r.robotCode LIKE %:keyword% OR " +
           "r.robotName LIKE %:keyword% OR " +
           "r.department LIKE %:keyword%)")
    List<Robot> searchRobots(@Param("keyword") String keyword);
    
    /**
     * 根据负责人 ID 查询机器人
     */
    List<Robot> findByOwnerId(Long ownerId);
}
