-- PréstamoLab CTMA: inventario de equipos del instructor (HU-12).
-- Ejecutar en Supabase > SQL Editor después de 004. Es seguro repetirlo.

-- La app elimina equipos con DELETE; 004 solo concedió select, insert y update.
-- Sin este permiso PostgREST responde 401 a la anon key y la app cierra la sesión.
-- Riesgo aceptado hasta el Sprint 9: ver docs/RIESGOS.md (R-05).
grant delete on public.equipments to anon;

-- La política "equipments_anon" de 004 ya es "for all", así que cubre el DELETE.
-- loans.equipment_id sigue protegiendo el historial: un equipo con préstamos no se puede
-- borrar (la app tampoco lo intenta, CA-HU12-04).
