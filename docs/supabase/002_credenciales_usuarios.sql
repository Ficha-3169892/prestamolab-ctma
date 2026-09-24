-- PréstamoLab CTMA: credenciales para el inicio de sesión (HU-10) contra public.users.
-- Ejecutar en Supabase > SQL Editor después de 001. Es seguro repetirlo.
-- La app busca por email (si el identificador tiene "@") o por document, junto con el
-- SHA-256 de la contraseña (hex en minúscula), y guarda en DataStore user_id, email,
-- role y full_name. La contraseña nunca se guarda ni se envía en texto plano.

alter table public.users add column if not exists document varchar(20);
alter table public.users add column if not exists password_hash varchar(255);
-- Columna de una versión anterior de este script que guardaba la clave en texto plano
alter table public.users drop column if exists password;

alter table public.users drop constraint if exists users_document_key;
alter table public.users add constraint users_document_key unique (document);

-- ── Usuarios de prueba (clave: 123456) ───────────────────────────────────────
-- 8d969eef... = SHA-256('123456'); se puede verificar con encode(digest('123456', 'sha256'), 'hex')
insert into public.users (id, email, document, full_name, role, password_hash) values
    (gen_random_uuid(), 'instructor@sena.edu.co', '12345', 'Instructor CTMA', 'INSTRUCTOR',
     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'),
    (gen_random_uuid(), 'estudiante@sena.edu.co', '67890', 'Estudiante CTMA', 'ESTUDIANTE',
     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92')
on conflict (document) do update set
    email = excluded.email,
    full_name = excluded.full_name,
    role = excluded.role,
    password_hash = excluded.password_hash;
