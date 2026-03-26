package com.rpa.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long permId;

    @Column(name = "perm_name", nullable = false, length = 50)
    private String permName;

    @Column(name = "perm_key", nullable = false, unique = true, length = 100)
    private String permKey;

    @Column(name = "perm_type", nullable = false)
    private Integer permType;

    @Column(name = "parent_id")
    private Long parentId = 0L;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;
}
