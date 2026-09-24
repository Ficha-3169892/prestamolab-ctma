-- PréstamoLab CTMA: lo que necesita la sincronización Room ↔ Supabase (HU-07).
-- Ejecutar en Supabase > SQL Editor después de 003. Es seguro repetirlo.
-- Supone que loans y returns siguen vacías (así estaban el 2026-09-24).

-- ── loans.user_id: text → uuid con FK a users ────────────────────────────────
-- La app trae el nombre del solicitante con users!loans_user_id_fkey(full_name),
-- así que la restricción debe llamarse exactamente así.
alter table public.loans drop constraint if exists loans_user_id_fkey;
alter table public.loans alter column user_id type uuid using user_id::uuid;
alter table public.loans add constraint loans_user_id_fkey
    foreign key (user_id) references public.users(id);

-- ── Un préstamo se devuelve una sola vez (igual que el índice único de Room) ──
alter table public.returns drop constraint if exists returns_loan_id_key;
alter table public.returns add constraint returns_loan_id_key unique (loan_id);

-- ── Acceso con la anon key ───────────────────────────────────────────────────
-- La app lee las tres tablas y hace upsert (INSERT ... ON CONFLICT DO UPDATE).
-- Riesgo aceptado hasta el Sprint 9: ver docs/RIESGOS.md (R-03, R-05).
grant select, insert, update on public.equipments, public.loans, public.returns to anon;

alter table public.equipments enable row level security;
alter table public.loans enable row level security;
alter table public.returns enable row level security;

drop policy if exists "equipments_anon" on public.equipments;
create policy "equipments_anon" on public.equipments for all to anon using (true) with check (true);
drop policy if exists "loans_anon" on public.loans;
create policy "loans_anon" on public.loans for all to anon using (true) with check (true);
drop policy if exists "returns_anon" on public.returns;
create policy "returns_anon" on public.returns for all to anon using (true) with check (true);

-- ── Catálogo inicial ─────────────────────────────────────────────────────────
-- Mismos uuid que CatalogoInicial en la app: los equipos que la versión 1 creaba en el
-- teléfono se reconocen como estos y no se duplican al sincronizar.
insert into public.equipments (id, name, category, status) values
    ('0b1e0000-0000-4000-8000-000000000001', 'Multímetro Digital', 'Herramienta', 'DISPONIBLE'),
    ('0b1e0000-0000-4000-8000-000000000002', 'Osciloscopio 100MHz', 'Laboratorio', 'RESERVADO'),
    ('0b1e0000-0000-4000-8000-000000000003', 'Cautín Estación de Soldadura', 'Herramienta', 'DISPONIBLE'),
    ('0b1e0000-0000-4000-8000-000000000004', 'Fuente de Poder DC', 'Laboratorio', 'DISPONIBLE'),
    ('0b1e0000-0000-4000-8000-000000000005', 'Kit Arduino Uno', 'Herramienta', 'PRESTADO')
on conflict (id) do update set
    name = excluded.name,
    category = excluded.category,
    status = excluded.status;

-- ── Préstamos de demostración del estudiante de prueba ───────────────────────
-- #1 SOLICITADA (reserva el osciloscopio) y #2 PRESTADO (permite probar la devolución).
insert into public.loans (id, user_id, equipment_id, status, request_date, return_date,
                          environment, purpose, duration_hours)
select v.id::uuid, u.id, v.equipo::uuid, v.estado, v.inicio::timestamptz, v.limite::timestamptz,
       v.ambiente, v.proposito, v.horas
from public.users u
cross join (values
    ('5eed0000-0000-4000-8000-000000000001', '0b1e0000-0000-4000-8000-000000000002', 'SOLICITADA',
     '2026-09-02 08:00-05', '2026-09-02 10:00-05', 'Laboratorio 302', 'Práctica de señales', 2),
    ('5eed0000-0000-4000-8000-000000000002', '0b1e0000-0000-4000-8000-000000000005', 'PRESTADO',
     '2026-09-03 08:00-05', '2026-09-03 12:00-05', 'Ambiente de Electrónica', 'Prototipo de sensores IoT', 4)
) as v(id, equipo, estado, inicio, limite, ambiente, proposito, horas)
where u.email = 'estudiante@sena.edu.co'
on conflict (id) do nothing;
