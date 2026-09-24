-- PréstamoLab CTMA: evidencia fotográfica (HU-08).
-- Ejecutar en Supabase > SQL Editor después de 007. Es seguro repetirlo.
-- La tabla public.evidences ya existe (001_columnas_faltantes.sql).

-- ── Bucket de Storage ────────────────────────────────────────────────────────
-- Público para lectura: la app guarda en evidences.photo_url la URL pública de cada foto.
-- Solo imágenes JPEG de hasta 15 MB (fotos originales de la cámara, con su EXIF).
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('evidencias', 'evidencias', true, 15728640, array['image/jpeg'])
on conflict (id) do update
    set public = excluded.public,
        file_size_limit = excluded.file_size_limit,
        allowed_mime_types = excluded.allowed_mime_types;

-- La app sube con x-upsert (reintentar una subida interrumpida no falla por duplicado),
-- y el upsert de Storage exige permisos de insert, select y update sobre storage.objects.
drop policy if exists "evidencias_anon_insert" on storage.objects;
create policy "evidencias_anon_insert" on storage.objects
    for insert to anon with check (bucket_id = 'evidencias');
drop policy if exists "evidencias_anon_select" on storage.objects;
create policy "evidencias_anon_select" on storage.objects
    for select to anon using (bucket_id = 'evidencias');
drop policy if exists "evidencias_anon_update" on storage.objects;
create policy "evidencias_anon_update" on storage.objects
    for update to anon using (bucket_id = 'evidencias') with check (bucket_id = 'evidencias');

-- ── Tabla evidences ──────────────────────────────────────────────────────────
-- La app registra cada evidencia con upsert (INSERT ... ON CONFLICT DO UPDATE).
-- Sin estos permisos PostgREST responde 401 a la anon key y la app cierra la sesión.
-- Riesgo aceptado hasta el Sprint 9: ver docs/RIESGOS.md (R-05).
grant select, insert, update on public.evidences to anon;

alter table public.evidences enable row level security;
drop policy if exists "evidences_anon" on public.evidences;
create policy "evidences_anon" on public.evidences for all to anon using (true) with check (true);
