-- PréstamoLab CTMA: devuelve los datos de demostración al estado inicial de 004.
-- Ejecutar en Supabase > SQL Editor antes de una demo o prueba manual. Solo toca los
-- préstamos de demostración (5eed...) y los 5 equipos del catálogo inicial (0b1e...); los
-- préstamos, equipos y actividades creados durante las pruebas se conservan.

delete from public.returns
where loan_id in ('5eed0000-0000-4000-8000-000000000001', '5eed0000-0000-4000-8000-000000000002');

-- Evidencias de los préstamos de demostración (HU-08). Las fotos quedan en el bucket "evidencias":
-- Supabase no permite borrarlas con SQL; se borran en Storage > evidencias > carpeta 5eed...
delete from public.evidences
where loan_id in ('5eed0000-0000-4000-8000-000000000001', '5eed0000-0000-4000-8000-000000000002');

-- También se limpia la revisión del instructor (HU-14): una solicitud nueva no tiene revisor
update public.loans set status = 'SOLICITADA', reviewed_by = null, rejection_reason = null
where id = '5eed0000-0000-4000-8000-000000000001';
update public.loans set status = 'PRESTADO', reviewed_by = null, rejection_reason = null
where id = '5eed0000-0000-4000-8000-000000000002';

-- Estado, nombre y categoría originales (el instructor puede haberlos editado en HU-12)
update public.equipments as e set
    status = v.status, name = v.name, title = v.name, category = v.category
from (values
    ('0b1e0000-0000-4000-8000-000000000001'::uuid, 'Multímetro Digital', 'Herramienta', 'DISPONIBLE'),
    ('0b1e0000-0000-4000-8000-000000000002'::uuid, 'Osciloscopio 100MHz', 'Laboratorio', 'RESERVADO'),
    ('0b1e0000-0000-4000-8000-000000000003'::uuid, 'Cautín Estación de Soldadura', 'Herramienta', 'DISPONIBLE'),
    ('0b1e0000-0000-4000-8000-000000000004'::uuid, 'Fuente de Poder DC', 'Laboratorio', 'DISPONIBLE'),
    ('0b1e0000-0000-4000-8000-000000000005'::uuid, 'Kit Arduino Uno', 'Herramienta', 'PRESTADO')
) as v(id, name, category, status)
where e.id = v.id;
