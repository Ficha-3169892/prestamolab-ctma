-- PréstamoLab CTMA: seguridad en el servidor (Sprint 9; trata R-01 a R-05 de docs/RIESGOS.md).
-- Ejecutar en Supabase > SQL Editor DESPUÉS de 001 a 008 y ANTES de instalar la versión de la app que envía
-- la cabecera x-sesion: las versiones anteriores dejan de poder iniciar sesión.
-- Es seguro repetirlo. Para volver atrás en una emergencia: emergencia/revertir_009_seguridad.sql (NO se ejecuta
-- como parte de la secuencia 001 a 009).
--
-- Sin Supabase Auth, la identidad se prueba con un token de sesión que emite iniciar_sesion() y que la app
-- envía en la cabecera x-sesion; las políticas RLS la leen con usuario_actual() y rol_actual().

create extension if not exists pgcrypto with schema extensions;

-- ═══ 1. Contraseñas (R-01, R-02) ═════════════════════════════════════════════
-- La app sigue enviando SHA-256 (la contraseña no viaja ni se guarda en claro). El servidor guarda
-- bcrypt(SHA-256), con sal: quien robe la tabla obtiene bcrypt, que no sirve para iniciar sesión.
alter table public.users add column if not exists password_bcrypt text;
do $$
begin
    if exists (select 1 from information_schema.columns
               where table_schema = 'public' and table_name = 'users' and column_name = 'password_hash') then
        update public.users
        set password_bcrypt = extensions.crypt(password_hash, extensions.gen_salt('bf'))
        where password_bcrypt is null and password_hash is not null;
    end if;
end $$;
-- El SHA-256 guardado era justamente lo que permitía entrar sin la contraseña
alter table public.users drop column if exists password_hash;

-- ═══ 2. Sesiones en el servidor (R-04) ═══════════════════════════════════════
create table if not exists public.sesiones (
    token uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.users(id) on delete cascade,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null default now() + interval '7 days'
);
alter table public.sesiones enable row level security;
-- Sin políticas: la anon key no puede leer ni escribir tokens; solo las funciones de abajo
revoke all on public.sesiones from anon, authenticated;

-- Usuario dueño del token de la cabecera x-sesion; null si falta, no es válido o venció.
create or replace function public.usuario_actual() returns uuid
language plpgsql stable security definer set search_path = public as $$
declare
    v_token text := nullif(current_setting('request.headers', true), '')::json ->> 'x-sesion';
    v_usuario uuid;
begin
    if v_token is null or v_token !~ '^[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}$' then
        return null;
    end if;
    select s.user_id into v_usuario
    from public.sesiones s
    where s.token = v_token::uuid and s.expires_at > now();
    return v_usuario;
end $$;

create or replace function public.rol_actual() returns text
language sql stable security definer set search_path = public as $$
    select u.role::text from public.users u where u.id = public.usuario_actual()
$$;

-- La app lo consulta antes de sincronizar: con RLS, una sesión vencida no da error al leer sino
-- listas vacías, y la app no debe confundir eso con "ya no hay datos".
create or replace function public.sesion_valida() returns boolean
language sql stable security definer set search_path = public as $$
    select public.usuario_actual() is not null
$$;

-- Login (HU-10): correo si trae "@", si no documento; p_hash es el SHA-256 que calcula la app.
create or replace function public.iniciar_sesion(p_identificador text, p_hash text)
returns table (token uuid, id uuid, email text, full_name text, role text)
language plpgsql volatile security definer set search_path = public, extensions as $$
#variable_conflict use_column
declare
    v_usuario public.users%rowtype;
    v_token uuid;
begin
    select * into v_usuario
    from public.users u
    where (case when position('@' in p_identificador) > 0
                then lower(u.email) = lower(p_identificador)
                else u.document = p_identificador end)
      and u.password_bcrypt is not null
      and u.password_bcrypt = extensions.crypt(p_hash, u.password_bcrypt)
    limit 1;

    if not found then
        -- Frena los intentos por fuerza bruta sin revelar si el usuario existe
        perform pg_sleep(1);
        return;
    end if;

    delete from public.sesiones s where s.user_id = v_usuario.id and s.expires_at < now();
    insert into public.sesiones (user_id) values (v_usuario.id) returning sesiones.token into v_token;
    -- email, full_name y role son varchar en la tabla: se convierten al text que declara la función
    return query select v_token, v_usuario.id, v_usuario.email::text, v_usuario.full_name::text, v_usuario.role::text;
end $$;

create or replace function public.cerrar_sesion() returns void
language plpgsql volatile security definer set search_path = public as $$
begin
    -- usuario_actual() ya validó el formato del token: el cast no puede fallar
    if public.usuario_actual() is null then
        return;
    end if;
    delete from public.sesiones s
    where s.token = (nullif(current_setting('request.headers', true), '')::json ->> 'x-sesion')::uuid;
end $$;

revoke all on function public.usuario_actual(), public.rol_actual(), public.sesion_valida(),
    public.iniciar_sesion(text, text), public.cerrar_sesion() from public;
grant execute on function public.usuario_actual(), public.rol_actual(), public.sesion_valida(),
    public.iniciar_sesion(text, text), public.cerrar_sesion() to anon;

-- ═══ 2b. Solo las políticas de este script ═══════════════════════════════════
-- Las políticas se combinan con OR: basta una política permisiva creada a mano (p. ej. desde el panel de
-- Supabase, "Enable read access for all users") para anular todas las de abajo. Encontrado al verificar:
-- con una así, un estudiante pudo borrar un equipo. Se eliminan todas las que este script no define.
do $$
declare
    p record;
begin
    for p in
        select tablename, policyname from pg_policies
        where schemaname = 'public'
          and tablename in ('users', 'equipments', 'loans', 'returns', 'evidences', 'activities', 'sesiones')
          and policyname not in (
              'users_con_sesion',
              'equipments_ver', 'equipments_crear', 'equipments_cambiar', 'equipments_eliminar',
              'loans_ver', 'loans_crear', 'loans_cambiar',
              'returns_ver', 'returns_escribir', 'returns_cambiar',
              'evidences_ver', 'evidences_escribir', 'evidences_cambiar',
              'activities_ver', 'activities_crear', 'activities_cambiar', 'activities_eliminar'
          )
    loop
        execute format('drop policy %I on public.%I', p.policyname, p.tablename);
        raise notice 'Política eliminada: %.%', p.tablename, p.policyname;
    end loop;
end $$;

-- ═══ 3. users deja de ser legible (R-03) ═════════════════════════════════════
-- Solo id y nombre, y solo con sesión: los necesita "Solicitante" en los préstamos que ve el instructor.
revoke select on public.users from anon;
grant select (id, full_name) on public.users to anon;
-- Sin RLS activo la política de abajo no se aplica y id y full_name serían públicos
alter table public.users enable row level security;
drop policy if exists "users_select_publico" on public.users;
drop policy if exists "users_con_sesion" on public.users;
create policy "users_con_sesion" on public.users for select to anon
    using (public.usuario_actual() is not null);

-- ═══ 4. RLS por rol (R-05) ═══════════════════════════════════════════════════
-- equipments: todos los ven; el estudiante solo cambia el estado (al reservar o devolver, ver trigger)
drop policy if exists "equipments_anon" on public.equipments;
drop policy if exists "equipments_ver" on public.equipments;
drop policy if exists "equipments_crear" on public.equipments;
drop policy if exists "equipments_cambiar" on public.equipments;
drop policy if exists "equipments_eliminar" on public.equipments;
create policy "equipments_ver" on public.equipments for select to anon
    using (public.usuario_actual() is not null);
create policy "equipments_crear" on public.equipments for insert to anon
    with check (public.rol_actual() = 'INSTRUCTOR');
create policy "equipments_cambiar" on public.equipments for update to anon
    using (public.usuario_actual() is not null) with check (public.usuario_actual() is not null);
create policy "equipments_eliminar" on public.equipments for delete to anon
    using (public.rol_actual() = 'INSTRUCTOR');

-- loans: el instructor ve y revisa todos; el estudiante solo los suyos
drop policy if exists "loans_anon" on public.loans;
drop policy if exists "loans_ver" on public.loans;
drop policy if exists "loans_crear" on public.loans;
drop policy if exists "loans_cambiar" on public.loans;
create policy "loans_ver" on public.loans for select to anon
    using (public.rol_actual() = 'INSTRUCTOR' or user_id = public.usuario_actual());
-- El upsert del instructor al aprobar también pasa por la política de insert
create policy "loans_crear" on public.loans for insert to anon
    with check (public.rol_actual() = 'INSTRUCTOR' or user_id = public.usuario_actual());
create policy "loans_cambiar" on public.loans for update to anon
    using (public.rol_actual() = 'INSTRUCTOR' or user_id = public.usuario_actual())
    with check (public.rol_actual() = 'INSTRUCTOR' or user_id = public.usuario_actual());

-- returns y evidences: del dueño del préstamo; el instructor las consulta
drop policy if exists "returns_anon" on public.returns;
drop policy if exists "returns_ver" on public.returns;
drop policy if exists "returns_escribir" on public.returns;
drop policy if exists "returns_cambiar" on public.returns;
create policy "returns_ver" on public.returns for select to anon
    using (public.rol_actual() = 'INSTRUCTOR'
        or exists (select 1 from public.loans l where l.id = loan_id and l.user_id = public.usuario_actual()));
create policy "returns_escribir" on public.returns for insert to anon
    with check (exists (select 1 from public.loans l where l.id = loan_id and l.user_id = public.usuario_actual()));
create policy "returns_cambiar" on public.returns for update to anon
    using (exists (select 1 from public.loans l where l.id = loan_id and l.user_id = public.usuario_actual()))
    with check (exists (select 1 from public.loans l where l.id = loan_id and l.user_id = public.usuario_actual()));

drop policy if exists "evidences_anon" on public.evidences;
drop policy if exists "evidences_ver" on public.evidences;
drop policy if exists "evidences_escribir" on public.evidences;
drop policy if exists "evidences_cambiar" on public.evidences;
create policy "evidences_ver" on public.evidences for select to anon
    using (public.rol_actual() = 'INSTRUCTOR'
        or exists (select 1 from public.loans l where l.id = loan_id and l.user_id = public.usuario_actual()));
create policy "evidences_escribir" on public.evidences for insert to anon
    with check (exists (select 1 from public.loans l where l.id = loan_id and l.user_id = public.usuario_actual()));
create policy "evidences_cambiar" on public.evidences for update to anon
    using (exists (select 1 from public.loans l where l.id = loan_id and l.user_id = public.usuario_actual()))
    with check (exists (select 1 from public.loans l where l.id = loan_id and l.user_id = public.usuario_actual()));

-- activities: todos las consultan; solo el instructor las mantiene (HU-11)
drop policy if exists "activities_anon" on public.activities;
drop policy if exists "activities_ver" on public.activities;
drop policy if exists "activities_crear" on public.activities;
drop policy if exists "activities_cambiar" on public.activities;
drop policy if exists "activities_eliminar" on public.activities;
create policy "activities_ver" on public.activities for select to anon
    using (public.usuario_actual() is not null);
create policy "activities_crear" on public.activities for insert to anon
    with check (public.rol_actual() = 'INSTRUCTOR');
create policy "activities_cambiar" on public.activities for update to anon
    using (public.rol_actual() = 'INSTRUCTOR') with check (public.rol_actual() = 'INSTRUCTOR');
create policy "activities_eliminar" on public.activities for delete to anon
    using (public.rol_actual() = 'INSTRUCTOR');

-- ═══ 5. Reglas del negocio en el servidor ════════════════════════════════════
-- RLS decide QUÉ filas; los triggers, QUÉ cambios. Un error 23514 llega a la app como 400: el registro
-- queda en ERROR y el usuario ve el aviso, sin cerrar la sesión.
-- Los triggers corren con los permisos de quien llama (no SECURITY DEFINER) para distinguir la app (rol anon)
-- del SQL Editor (administrador), que puede corregir datos, p. ej. con 005_reiniciar_demo.sql.

-- Un estudiante no se aprueba a sí mismo ni altera la revisión del instructor (HU-14)
create or replace function public.validar_cambio_prestamo() returns trigger
language plpgsql set search_path = public as $$
begin
    if current_user <> 'anon' or public.rol_actual() = 'INSTRUCTOR' then
        return new;
    end if;
    if tg_op = 'INSERT' then
        -- En un upsert de una fila propia existente, la validación real ocurre en el UPDATE. Con RLS, una fila
        -- ajena no es visible: se valida como inserción y luego la política de UPDATE la rechaza
        if exists (select 1 from public.loans where id = new.id) then
            return new;
        end if;
        if new.status <> 'SOLICITADA' or new.reviewed_by is not null or new.rejection_reason is not null then
            raise exception 'Un estudiante solo crea solicitudes en estado SOLICITADA' using errcode = '23514';
        end if;
        return new;
    end if;
    if new.reviewed_by is distinct from old.reviewed_by or new.rejection_reason is distinct from old.rejection_reason then
        raise exception 'Solo el instructor revisa solicitudes' using errcode = '23514';
    end if;
    if new.status is distinct from old.status and not (
        (old.status = 'SOLICITADA' and new.status = 'CANCELADA') or
        (old.status = 'PRESTADO' and new.status = 'DEVUELTO')
    ) then
        raise exception 'Transición no permitida para un estudiante: % -> %', old.status, new.status using errcode = '23514';
    end if;
    return new;
end $$;

drop trigger if exists validar_cambio_prestamo on public.loans;
create trigger validar_cambio_prestamo before insert or update on public.loans
    for each row execute function public.validar_cambio_prestamo();

-- El estudiante solo cambia el estado de un equipo, nunca su nombre ni su categoría (HU-12)
create or replace function public.validar_cambio_equipo() returns trigger
language plpgsql set search_path = public as $$
begin
    if current_user <> 'anon' or public.rol_actual() = 'INSTRUCTOR' then
        return new;
    end if;
    if new.name is distinct from old.name or new.title is distinct from old.title
        or new.category is distinct from old.category then
        raise exception 'Solo el instructor edita el inventario' using errcode = '23514';
    end if;
    return new;
end $$;

drop trigger if exists validar_cambio_equipo on public.equipments;
create trigger validar_cambio_equipo before update on public.equipments
    for each row execute function public.validar_cambio_equipo();
