package com.hacker.code.infrastructure.auth;

import lombok.Data;

import java.util.List;

@Data
public class AuthUser {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private Integer status;
    private List<String> roles;
}
