-- PréstamoLab CTMA: permite que la app lea public.users con la anon key (login de HU-10).
-- Ejecutar en Supabase > SQL Editor después de 002. Es seguro repetirlo.
-- Sin esto, PostgREST devuelve [] y el login responde "Usuario o contraseña incorrectos".
-- Riesgo aceptado: la tabla completa (incluido password_hash) queda legible; ver docs/RIESGOS.md (R-03).

grant select on public.users to anon;

alter table public.users enable row level security;

drop policy if exists "users_select_publico" on public.users;
create policy "users_select_publico" on public.users
    for select to anon
    using (true);
