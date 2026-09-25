-- NO FORMA PARTE DE LA SECUENCIA 001 a 009: no ejecutar al preparar la base de datos.
-- PréstamoLab CTMA: SOLO PARA EMERGENCIAS. Deshace 009_seguridad.sql y vuelve al acceso abierto de 003 a 008
-- (riesgos R-01 a R-05 otra vez abiertos). Úsalo si hay que volver a una versión de la app sin la cabecera
-- x-sesion. Es seguro repetirlo.

-- Triggers y políticas por rol
drop trigger if exists validar_cambio_prestamo on public.loans;
drop trigger if exists validar_cambio_equipo on public.equipments;
drop function if exists public.validar_cambio_prestamo();
drop function if exists public.validar_cambio_equipo();

drop policy if exists "equipments_ver" on public.equipments;
drop policy if exists "equipments_crear" on public.equipments;
drop policy if exists "equipments_cambiar" on public.equipments;
drop policy if exists "equipments_eliminar" on public.equipments;
drop policy if exists "loans_ver" on public.loans;
drop policy if exists "loans_crear" on public.loans;
drop policy if exists "loans_cambiar" on public.loans;
drop policy if exists "returns_ver" on public.returns;
drop policy if exists "returns_escribir" on public.returns;
drop policy if exists "returns_cambiar" on public.returns;
drop policy if exists "evidences_ver" on public.evidences;
drop policy if exists "evidences_escribir" on public.evidences;
drop policy if exists "evidences_cambiar" on public.evidences;
drop policy if exists "activities_ver" on public.activities;
drop policy if exists "activities_crear" on public.activities;
drop policy if exists "activities_cambiar" on public.activities;
drop policy if exists "activities_eliminar" on public.activities;

create policy "equipments_anon" on public.equipments for all to anon using (true) with check (true);
create policy "loans_anon" on public.loans for all to anon using (true) with check (true);
create policy "returns_anon" on public.returns for all to anon using (true) with check (true);
create policy "evidences_anon" on public.evidences for all to anon using (true) with check (true);
create policy "activities_anon" on public.activities for all to anon using (true) with check (true);

-- users vuelve a ser legible y a tener password_hash (SHA-256). bcrypt no se puede revertir:
-- se siembra de nuevo la clave de prueba 123456 de los dos usuarios de demostración (002).
drop policy if exists "users_con_sesion" on public.users;
grant select on public.users to anon;
create policy "users_select_publico" on public.users for select to anon using (true);
alter table public.users add column if not exists password_hash varchar(255);
update public.users
set password_hash = '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'
where document in ('12345', '67890');

-- Sesiones y funciones de login
drop function if exists public.iniciar_sesion(text, text);
drop function if exists public.cerrar_sesion();
drop function if exists public.sesion_valida();
drop function if exists public.rol_actual();
drop function if exists public.usuario_actual();
drop table if exists public.sesiones;
