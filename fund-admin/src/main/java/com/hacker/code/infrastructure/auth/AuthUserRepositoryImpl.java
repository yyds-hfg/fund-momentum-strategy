package com.hacker.code.infrastructure.auth;

import com.hacker.code.infrastructure.mapper.SysRoleMapper;
import com.hacker.code.infrastructure.mapper.SysUserMapper;
import com.hacker.code.infrastructure.persistence.po.SysRolePO;
import com.hacker.code.infrastructure.persistence.po.SysUserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AuthUserRepositoryImpl implements AuthUserRepository {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        SysUserPO userPO = sysUserMapper.selectByUsername(username);
        if (userPO == null) {
            return Optional.empty();
        }

        List<SysRolePO> rolePOs = sysRoleMapper.selectRolesByUserId(userPO.getId());

        AuthUser user = new AuthUser();
        user.setId(userPO.getId());
        user.setUsername(userPO.getUsername());
        user.setPassword(userPO.getPassword());
        user.setNickname(userPO.getNickname());
        user.setStatus(userPO.getStatus());
        user.setRoles(rolePOs.stream().map(SysRolePO::getRoleCode).collect(Collectors.toList()));
        return Optional.of(user);
    }
}
