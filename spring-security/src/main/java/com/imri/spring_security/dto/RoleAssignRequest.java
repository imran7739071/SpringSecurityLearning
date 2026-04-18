package com.imri.spring_security.dto;

import lombok.Data;

@Data
public class RoleAssignRequest {
    private String roleName;   // e.g. "ROLE_ADMIN"
}