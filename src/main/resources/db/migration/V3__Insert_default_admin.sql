-- Insert default admin user
-- Password: admin (BCrypt encoded)
-- You should change this password after first login!
INSERT INTO users (username, password, email, first_name, last_name, is_active, is_staff, is_superuser, date_joined)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- BCrypt hash of 'admin'
    'admin@sopds.local',
    'Admin',
    'User',
    true,
    true,
    true,
    CURRENT_TIMESTAMP
);

-- Insert default admin group
INSERT INTO groups (name, description)
VALUES (
    'Administrators',
    'Full system access'
);

-- Insert default user group
INSERT INTO groups (name, description)
VALUES (
    'Users',
    'Regular users with basic access'
);

-- Assign admin to Administrators group
INSERT INTO user_groups (user_id, group_id)
SELECT u.id, g.id
FROM users u, groups g
WHERE u.username = 'admin' AND g.name = 'Administrators';

-- Insert default permissions for Administrators group
INSERT INTO group_permissions (group_id, permission)
SELECT g.id, permission
FROM groups g, (
    VALUES
        ('BOOK_READ'),
        ('BOOK_WRITE'),
        ('BOOK_DELETE'),
        ('AUTHOR_READ'),
        ('AUTHOR_WRITE'),
        ('AUTHOR_DELETE'),
        ('GENRE_READ'),
        ('GENRE_WRITE'),
        ('GENRE_DELETE'),
        ('SERIES_READ'),
        ('SERIES_WRITE'),
        ('SERIES_DELETE'),
        ('USER_READ'),
        ('USER_WRITE'),
        ('USER_DELETE'),
        ('GROUP_READ'),
        ('GROUP_WRITE'),
        ('GROUP_DELETE'),
        ('SCAN_LIBRARY'),
        ('SYSTEM_CONFIG')
) AS perms(permission)
WHERE g.name = 'Administrators';

-- Insert default permissions for Users group
INSERT INTO group_permissions (group_id, permission)
SELECT g.id, permission
FROM groups g, (
    VALUES
        ('BOOK_READ'),
        ('AUTHOR_READ'),
        ('GENRE_READ'),
        ('SERIES_READ')
) AS perms(permission)
WHERE g.name = 'Users';