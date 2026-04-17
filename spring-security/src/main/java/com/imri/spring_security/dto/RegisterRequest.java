// dto/RegisterRequest.java
package com.imri.spring_security.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}