-- Seed service-account user needed by aladdin-grc-core-service for startup token refresh

DO $$
DECLARE
    v_role_id UUID := '00000000-0000-0000-0000-00000000a001'::uuid;
    v_user_id UUID := '00000000-0000-0000-0000-000000000004'::uuid;
BEGIN
    INSERT INTO role (id, name)
    VALUES (v_role_id, 'ADMIN')
    ON CONFLICT (name) DO NOTHING;

    INSERT INTO "user" (
        id,
        code,
        employee_id,
        full_name,
        email,
        password,
        status,
        lock,
        created_by,
        created_date,
        modified_by
    )
    VALUES (
        v_user_id,
        'GRC_SERVICE',
        'GRC_SERVICE',
        'GRC Service Account',
        'grc.service@appro.ae',
        '$2y$10$V/9fjyHi47VMVkgXt93Oq.Nn6XKop6zbY2GcULtbKihvO8dcjmnS2',
        1,
        false,
        'system',
        NOW(),
        'system'
    )
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO user_role (id, user_id, role_id)
    VALUES ('00000000-0000-0000-0000-00000000b001'::uuid, v_user_id, v_role_id)
    ON CONFLICT DO NOTHING;
END
$$;

