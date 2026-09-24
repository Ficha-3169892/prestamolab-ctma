-- PréstamoLab CTMA: columnas que la app necesita y que aún no existen.
-- Ejecutar en Supabase > SQL Editor. Es seguro repetirlo (IF NOT EXISTS).
-- Las tablas están vacías, por eso las columnas NOT NULL no requieren valores por defecto.

-- ── users ────────────────────────────────────────────────────────────────────
-- Nombre visible del usuario (solicitante en préstamos, instructor en actividades).
alter table public.users add column if not exists full_name text not null;

-- ── equipments ───────────────────────────────────────────────────────────────
-- Nombre del equipo: es lo que muestra el catálogo (HU-01) y el detalle (HU-02).
alter table public.equipments add column if not exists name text not null;
-- Marca de última modificación para la sincronización incremental (HU-07).
alter table public.equipments add column if not exists updated_at timestamptz not null default now();

-- ── activities ───────────────────────────────────────────────────────────────
-- Fecha y ambiente de la actividad formativa (CA-HU11-01 y CA-HU11-02).
alter table public.activities add column if not exists scheduled_at timestamptz;
alter table public.activities add column if not exists location text;
alter table public.activities add column if not exists updated_at timestamptz not null default now();

-- ── loans ────────────────────────────────────────────────────────────────────
-- Datos del formulario de solicitud (HU-03): ambiente, propósito y duración.
alter table public.loans add column if not exists environment text not null;
alter table public.loans add column if not exists purpose text not null;
alter table public.loans add column if not exists duration_hours integer not null;
-- Revisión del instructor (HU-14): quién revisó y motivo de rechazo.
alter table public.loans add column if not exists reviewed_by uuid references public.users(id);
alter table public.loans add column if not exists rejection_reason text;
alter table public.loans add column if not exists updated_at timestamptz not null default now();

-- ── returns ──────────────────────────────────────────────────────────────────
-- Estado en que se devuelve el equipo (CA-HU05-03).
alter table public.returns add column if not exists equipment_condition text not null;

-- ── evidences (tabla nueva, recomendada) ─────────────────────────────────────
-- HU-08 pide fotos al recibir y al devolver, posiblemente varias por préstamo;
-- returns.evidence_url solo admite una foto y solo en la devolución.
create table if not exists public.evidences (
    id uuid primary key default gen_random_uuid(),
    loan_id uuid not null references public.loans(id) on delete cascade,
    stage text not null,                -- ENTREGA o DEVOLUCION
    photo_url text not null,            -- URL pública en evidencias_bucket
    latitude double precision,
    longitude double precision,
    taken_at timestamptz not null default now()
);

-- ── Valores permitidos (recomendado) ─────────────────────────────────────────
-- La app usa estos valores exactos; las restricciones evitan datos inconsistentes.
alter table public.users drop constraint if exists users_role_check;
alter table public.users add constraint users_role_check
    check (role in ('INSTRUCTOR', 'ESTUDIANTE'));

alter table public.equipments drop constraint if exists equipments_status_check;
alter table public.equipments add constraint equipments_status_check
    check (status in ('DISPONIBLE', 'RESERVADO', 'PRESTADO'));

alter table public.loans drop constraint if exists loans_status_check;
alter table public.loans add constraint loans_status_check
    check (status in ('SOLICITADA', 'PRESTADO', 'DEVUELTO', 'RECHAZADA', 'CANCELADA'));

alter table public.loans drop constraint if exists loans_duration_hours_check;
alter table public.loans add constraint loans_duration_hours_check
    check (duration_hours between 1 and 8);

alter table public.returns drop constraint if exists returns_equipment_condition_check;
alter table public.returns add constraint returns_equipment_condition_check
    check (equipment_condition in ('BUENO', 'CON_NOVEDAD', 'DANADO'));

alter table public.evidences drop constraint if exists evidences_stage_check;
alter table public.evidences add constraint evidences_stage_check
    check (stage in ('ENTREGA', 'DEVOLUCION'));
