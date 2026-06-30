package com.hacker.code.adapter.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String username;
    private String nickname;
    private List<String> roles;
}
