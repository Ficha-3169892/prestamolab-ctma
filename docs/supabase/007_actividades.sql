-- PréstamoLab CTMA: actividades formativas (HU-11).
-- Ejecutar en Supabase > SQL Editor después de 006. Es seguro repetirlo.

-- Todos reciben las actividades al sincronizar; el instructor las crea y edita con upsert
-- (INSERT ... ON CONFLICT DO UPDATE) y las elimina con DELETE.
-- Sin estos permisos PostgREST responde 401 a la anon key y la app cierra la sesión.
-- Riesgo aceptado hasta el Sprint 9: ver docs/RIESGOS.md (R-05).
grant select, insert, update, delete on public.activities to anon;

alter table public.activities enable row level security;
drop policy if exists "activities_anon" on public.activities;
create policy "activities_anon" on public.activities for all to anon using (true) with check (true);
