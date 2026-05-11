--liquibase formatted sql

--changeset akarmanov:ronin-user-1
INSERT INTO sso.users(email, username, password_hash, full_name, active, account_non_locked)
VALUES ('ronin@ronin.com', 'ronin',
        '$2a$10$iHkWzHmHkqY1EkJFNJdTZezEJH7ZCv8mLkoybRNxaW8fFbxDmJqJe',
        'Ронин', false, true);

--changeset akarmanov:ronin-user-2
INSERT INTO sso.user_roles(user_id, role_id)
SELECT u.user_id, r.role_id
FROM sso.users u
CROSS JOIN sso.roles r
WHERE u.username = 'ronin' 
  AND r.role_code = 'TRACKER'
  AND NOT EXISTS (
    SELECT 1 FROM sso.user_roles ur 
    WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id
  );