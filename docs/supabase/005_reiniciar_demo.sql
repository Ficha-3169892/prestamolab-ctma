-- PréstamoLab CTMA: devuelve los datos de demostración al estado inicial de 004.
-- Ejecutar en Supabase > SQL Editor antes de una demo o prueba manual. Solo toca los
-- préstamos de demostración (5eed...) y los 5 equipos del catálogo inicial (0b1e...).

delete from public.returns
where loan_id in ('5eed0000-0000-4000-8000-000000000001', '5eed0000-0000-4000-8000-000000000002');

update public.loans set status = 'SOLICITADA' where id = '5eed0000-0000-4000-8000-000000000001';
update public.loans set status = 'PRESTADO'   where id = '5eed0000-0000-4000-8000-000000000002';

update public.equipments set status = case id
    when '0b1e0000-0000-4000-8000-000000000002' then 'RESERVADO'
    when '0b1e0000-0000-4000-8000-000000000005' then 'PRESTADO'
    else 'DISPONIBLE'
end
where id in (
    '0b1e0000-0000-4000-8000-000000000001', '0b1e0000-0000-4000-8000-000000000002',
    '0b1e0000-0000-4000-8000-000000000003', '0b1e0000-0000-4000-8000-000000000004',
    '0b1e0000-0000-4000-8000-000000000005'
);
