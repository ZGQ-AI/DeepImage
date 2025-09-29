
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    delete_flag SMALLINT DEFAULT 0,
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'User information table, stores basic information of system users';
COMMENT ON COLUMN users.id IS 'User unique identifier, primary key';
COMMENT ON COLUMN users.username IS 'Username for login, globally unique';
COMMENT ON COLUMN users.email IS 'Email address for login and notifications, globally unique';
COMMENT ON COLUMN users.password_hash IS 'Password hash value, encrypted by application layer';
COMMENT ON COLUMN users.first_name IS 'User first name';
COMMENT ON COLUMN users.last_name IS 'User last name';
COMMENT ON COLUMN users.phone IS 'Phone number';
COMMENT ON COLUMN users.avatar_url IS 'Avatar image URL address';
COMMENT ON COLUMN users.delete_flag IS 'Delete flag: 0=not deleted, 1=deleted';
COMMENT ON COLUMN users.verified IS 'Email verification status: false=not verified, true=verified';
COMMENT ON COLUMN users.created_at IS 'Account creation time';
COMMENT ON COLUMN users.updated_at IS 'Account information last update time';

-- users表索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(100),
    delete_flag SMALLINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL
);

COMMENT ON TABLE roles IS 'Role information table, defines roles in the system';
COMMENT ON COLUMN roles.id IS 'Role unique identifier, primary key';
COMMENT ON COLUMN roles.role_name IS 'Role display name, globally unique';
COMMENT ON COLUMN roles.role_code IS 'Role code for program logic, globally unique';
COMMENT ON COLUMN roles.description IS 'Role description information';
COMMENT ON COLUMN roles.delete_flag IS 'Delete flag: 0=not deleted, 1=deleted';
COMMENT ON COLUMN roles.created_at IS 'Role creation time';
COMMENT ON COLUMN roles.updated_at IS 'Role information last update time';
COMMENT ON COLUMN roles.created_by IS 'Creator user ID';
COMMENT ON COLUMN roles.updated_by IS 'Last updater user ID';

-- roles表索引
CREATE INDEX idx_roles_role_name ON roles(role_name);
CREATE INDEX idx_roles_role_code ON roles(role_code);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    permission_code VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(100),
    delete_flag SMALLINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL
);

COMMENT ON TABLE permissions IS 'Permission information table, defines permissions in the system';
COMMENT ON COLUMN permissions.id IS 'Permission unique identifier, primary key';
COMMENT ON COLUMN permissions.permission_name IS 'Permission display name, globally unique';
COMMENT ON COLUMN permissions.permission_code IS 'Permission code for program logic, globally unique';
COMMENT ON COLUMN permissions.description IS 'Permission description information';
COMMENT ON COLUMN permissions.delete_flag IS 'Delete flag: 0=not deleted, 1=deleted';
COMMENT ON COLUMN permissions.created_at IS 'Permission creation time';
COMMENT ON COLUMN permissions.updated_at IS 'Permission information last update time';
COMMENT ON COLUMN permissions.created_by IS 'Creator user ID';
COMMENT ON COLUMN permissions.updated_by IS 'Last updater user ID';

-- permissions表索引
CREATE INDEX idx_permissions_permission_name ON permissions(permission_name);
CREATE INDEX idx_permissions_permission_code ON permissions(permission_code);

CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    delete_flag SMALLINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, role_id)
);

COMMENT ON TABLE user_roles IS 'User role association table, establishes many-to-many relationship between users and roles';
COMMENT ON COLUMN user_roles.id IS 'Association record unique identifier, primary key';
COMMENT ON COLUMN user_roles.user_id IS 'User ID, references users table';
COMMENT ON COLUMN user_roles.role_id IS 'Role ID, references roles table';
COMMENT ON COLUMN user_roles.delete_flag IS 'Delete flag: 0=not deleted, 1=deleted';
COMMENT ON COLUMN user_roles.created_at IS 'Association relationship creation time';
COMMENT ON COLUMN user_roles.updated_at IS 'Association relationship last update time';

-- user_roles表索引
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

CREATE TABLE role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    delete_flag SMALLINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, permission_id)
);

COMMENT ON TABLE role_permissions IS 'Role permission association table, establishes many-to-many relationship between roles and permissions';
COMMENT ON COLUMN role_permissions.id IS 'Association record unique identifier, primary key';
COMMENT ON COLUMN role_permissions.role_id IS 'Role ID, references roles table';
COMMENT ON COLUMN role_permissions.permission_id IS 'Permission ID, references permissions table';
COMMENT ON COLUMN role_permissions.delete_flag IS 'Delete flag: 0=not deleted, 1=deleted';
COMMENT ON COLUMN role_permissions.created_at IS 'Association relationship creation time';
COMMENT ON COLUMN role_permissions.updated_at IS 'Association relationship last update time';

-- role_permissions表索引
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_used_at TIMESTAMP WITH TIME ZONE,
    revoked SMALLINT DEFAULT 0,
    delete_flag SMALLINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE refresh_tokens IS 'Refresh token table, manages refresh tokens';
COMMENT ON COLUMN refresh_tokens.id IS 'Refresh token unique identifier, primary key';
COMMENT ON COLUMN refresh_tokens.user_id IS 'User ID, references users table';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'Refresh token hash value, not stored in plain text, globally unique';
COMMENT ON COLUMN refresh_tokens.session_id IS 'Associated session ID, references sessions table';
COMMENT ON COLUMN refresh_tokens.expires_at IS 'Refresh token expiration time';
COMMENT ON COLUMN refresh_tokens.last_used_at IS 'Last used time, for activity statistics';
COMMENT ON COLUMN refresh_tokens.revoked IS 'Usage status: 0=not used, 1=used';
COMMENT ON COLUMN refresh_tokens.delete_flag IS 'Delete flag: 0=not deleted, 1=deleted';
COMMENT ON COLUMN refresh_tokens.created_at IS 'Token creation time';
COMMENT ON COLUMN refresh_tokens.updated_at IS 'Token information last update time';

CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    access_token_hash VARCHAR(255) NOT NULL,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent TEXT,
    active SMALLINT DEFAULT 1,
    last_accessed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    delete_flag SMALLINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sessions IS 'User session table, manages user login sessions and access tokens';
COMMENT ON COLUMN sessions.id IS 'Session unique identifier, primary key';
COMMENT ON COLUMN sessions.user_id IS 'User ID, references users table';
COMMENT ON COLUMN sessions.access_token_hash IS 'Access token hash value, not stored in plain text';
COMMENT ON COLUMN sessions.device_info IS 'Device information (device type, operating system, browser, etc.)';
COMMENT ON COLUMN sessions.ip_address IS 'Login IP address';
COMMENT ON COLUMN sessions.user_agent IS 'User agent string for device identification';
COMMENT ON COLUMN sessions.active IS 'Session active status: 0=revoked, 1=active';
COMMENT ON COLUMN sessions.last_accessed_at IS 'Last access time, for cleaning up long-unused sessions';
COMMENT ON COLUMN sessions.delete_flag IS 'Delete flag: 0=not deleted, 1=deleted';
COMMENT ON COLUMN sessions.created_at IS 'Session creation time';
COMMENT ON COLUMN sessions.updated_at IS 'Session information last update time';

-- refresh_tokens表索引
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_session_id ON refresh_tokens(session_id);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- sessions表索引
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_access_token_hash ON sessions(access_token_hash);
