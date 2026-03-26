package com.rpa.auth.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_role_permission")
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "perm_id", nullable = false)
    private Long permId;
}
