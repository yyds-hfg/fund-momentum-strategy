-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    nickname VARCHAR(50) COMMENT '昵称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码：admin/researcher/viewer',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 初始化角色
INSERT INTO sys_role (role_code, role_name) VALUES ('admin', '管理员')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
INSERT INTO sys_role (role_code, role_name) VALUES ('researcher', '研究员')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
INSERT INTO sys_role (role_code, role_name) VALUES ('viewer', '只读用户')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 初始化默认用户 admin/admin（密码使用 BCrypt 加密后的 'admin'）
INSERT INTO sys_user (username, password, nickname, status)
VALUES ('admin', '$2a$10$6QyIdCxMSkEjD.Mea7uBjuiVzOvQO.lMyzOKmZi2sJp0q5rDwJIu6', '管理员', 1)
ON DUPLICATE KEY UPDATE password = VALUES(password);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'admin' AND r.role_code = 'admin'
ON DUPLICATE KEY UPDATE user_id = user_id;
