package com.rpa.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String roleName;

    @Column(name = "remark", length = 100)
    private String remark;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;
}
