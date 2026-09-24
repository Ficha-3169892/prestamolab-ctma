-- PréstamoLab CTMA: credenciales para el inicio de sesión (HU-10) contra public.users.
-- Ejecutar en Supabase > SQL Editor después de 001. Es seguro repetirlo.
-- La app busca por email (si el identificador tiene "@") o por document, junto con password,
-- y guarda en DataStore el id, el email y el role del usuario encontrado.
-- Nota: la contraseña se guarda en texto plano y RLS está desactivado, así que la anon key
-- puede leerla. Es aceptable para el prototipo; se revisa en el Sprint 9 (OWASP ZAP).

alter table public.users add column if not exists document varchar(20);
alter table public.users add column if not exists password varchar(255) not null default '123456';

alter table public.users drop constraint if exists users_document_key;
alter table public.users add constraint users_document_key unique (document);

-- ── Usuarios de prueba ───────────────────────────────────────────────────────
insert into public.users (id, email, document, full_name, role, password)
select gen_random_uuid(), 'instructor@sena.edu.co', '12345', 'Instructor CTMA', 'INSTRUCTOR', '123'
where not exists (
    select 1 from public.users where email = 'instructor@sena.edu.co' or document = '12345'
);

insert into public.users (id, email, document, full_name, role, password)
select gen_random_uuid(), 'estudiante@sena.edu.co', '67890', 'Estudiante CTMA', 'ESTUDIANTE', '123'
where not exists (
    select 1 from public.users where email = 'estudiante@sena.edu.co' or document = '67890'
);
