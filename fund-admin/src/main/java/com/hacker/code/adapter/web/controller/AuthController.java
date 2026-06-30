package com.hacker.code.adapter.web.controller;

import com.hacker.code.adapter.web.dto.LoginRequest;
import com.hacker.code.adapter.web.dto.LoginResponse;
import com.hacker.code.infrastructure.auth.AuthUser;
import com.hacker.code.infrastructure.auth.AuthUserRepository;
import com.hacker.code.infrastructure.config.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthUser user = authUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("用户已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        return new LoginResponse(accessToken, refreshToken, user.getUsername(), user.getNickname(), user.getRoles());
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestHeader("Authorization") String authorization) {
        String token = extractToken(authorization);
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("无效的刷新令牌");
        }
        String username = jwtUtil.extractUsername(token);
        AuthUser user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        String accessToken = jwtUtil.generateAccessToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);
        return new LoginResponse(accessToken, refreshToken, user.getUsername(), user.getNickname(), user.getRoles());
    }

    @PostMapping("/logout")
    public void logout() {
        // 无状态 JWT，服务端无需处理；前端清除 token 即可
    }

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
