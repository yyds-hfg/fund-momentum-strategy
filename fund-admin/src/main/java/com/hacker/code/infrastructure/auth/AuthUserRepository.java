package com.hacker.code.infrastructure.auth;

import java.util.Optional;

public interface AuthUserRepository {

    Optional<AuthUser> findByUsername(String username);
}
