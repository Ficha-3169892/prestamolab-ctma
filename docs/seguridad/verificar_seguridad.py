"""
Verificación de seguridad del servidor (Sprint 9, riesgos R-01 a R-05 de docs/RIESGOS.md).

Actúa como un atacante que extrajo la anon key del APK: solo usa esa clave y las credenciales de los usuarios
de demostración (002). Cada prueba indica qué se espera; al final imprime el resumen.

    python docs/seguridad/verificar_seguridad.py

Requiere docs/supabase/009_seguridad.sql aplicado. Los ataques de escritura apuntan a los datos de
demostración y deben ser rechazados; el de borrado usa un equipo de prueba que el script crea y elimina.
"""
import hashlib
import json
import time
import urllib.error
import urllib.request
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[2]
PROPS = dict(
    linea.strip().split("=", 1)
    for linea in (RAIZ / "local.properties").read_text(encoding="utf-8").splitlines()
    if "=" in linea and not linea.startswith("#")
)
URL, CLAVE = PROPS["SUPABASE_URL"].rstrip("/"), PROPS["SUPABASE_ANON_KEY"]
HASH = hashlib.sha256(b"123456").hexdigest()
PRESTAMO_DEMO = "5eed0000-0000-4000-8000-000000000001"
PRESTAMO_DEMO_2 = "5eed0000-0000-4000-8000-000000000002"
EQUIPO_PRUEBA = "5ec00000-0000-4000-8000-00000000000e"

resultados = []


def pedir(metodo, ruta, cuerpo=None, token=None):
    """Devuelve (código HTTP, JSON o texto)."""
    cabeceras = {
        "apikey": CLAVE,
        "Authorization": f"Bearer {CLAVE}",
        "Content-Type": "application/json; charset=utf-8",
        "Prefer": "return=representation",
    }
    if token:
        cabeceras["x-sesion"] = token
    datos = json.dumps(cuerpo, ensure_ascii=False).encode("utf-8") if cuerpo is not None else None
    peticion = urllib.request.Request(f"{URL}/rest/v1/{ruta}", data=datos, method=metodo, headers=cabeceras)
    try:
        with urllib.request.urlopen(peticion) as r:
            texto = r.read().decode("utf-8")
            codigo = r.status
    except urllib.error.HTTPError as e:
        texto = e.read().decode("utf-8")
        codigo = e.code
    try:
        return codigo, json.loads(texto) if texto else None
    except json.JSONDecodeError:
        return codigo, texto


def comprobar(riesgo, descripcion, ok, detalle):
    resultados.append((riesgo, descripcion, ok))
    print(f"[{'OK ' if ok else 'FALLA'}] {riesgo:<6} {descripcion}\n         {str(detalle)[:160]}")


def login(identificador, hash_=HASH):
    return pedir("POST", "rpc/iniciar_sesion", {"p_identificador": identificador, "p_hash": hash_})


def filas(respuesta):
    return respuesta[1] if isinstance(respuesta[1], list) else []


# ── Sin sesión ────────────────────────────────────────────────────────────────
r = pedir("GET", "users?select=*")
comprobar("R-03", "users completo no se lee sin sesión", r[0] in (401, 403), r)
r = pedir("GET", "users?select=id,full_name")
comprobar("R-03", "id y nombre no se leen sin sesión", filas(r) == [], r)
r = pedir("GET", "users?select=password_hash")
comprobar("R-02", "ya no existe el SHA-256 guardado (pass-the-hash)", r[0] == 400, r)
r = pedir("GET", "sesiones?select=*")
comprobar("R-04", "los tokens de sesión no se leen", r[0] in (401, 403), r)
for tabla in ["equipments", "loans", "returns", "evidences", "activities"]:
    r = pedir("GET", f"{tabla}?select=id")
    comprobar("R-05", f"{tabla} no se lee sin sesión", filas(r) == [], r)

# ── Login ─────────────────────────────────────────────────────────────────────
inicio = time.monotonic()
r = login("estudiante@sena.edu.co", hashlib.sha256(b"otra").hexdigest())
comprobar("R-01", "clave incorrecta: sin sesión y con demora anti fuerza bruta",
          filas(r) == [] and time.monotonic() - inicio >= 0.9, f"{r} en {time.monotonic() - inicio:.1f} s")
r = login("estudiante@sena.edu.co", hashlib.sha256(HASH.encode()).hexdigest())
comprobar("R-02", "el hash del hash no sirve como contraseña", filas(r) == [], r)
estudiante = filas(login("estudiante@sena.edu.co"))
por_documento = filas(login("67890"))
instructor = filas(login("instructor@sena.edu.co"))
comprobar("HU-10", "login por correo, por documento y del instructor",
          len(estudiante) == len(por_documento) == len(instructor) == 1, "tokens emitidos")
TE, TI = estudiante[0]["token"], instructor[0]["token"]
ID_INSTRUCTOR = instructor[0]["id"]
ID_ESTUDIANTE = estudiante[0]["id"]

r = pedir("POST", "rpc/sesion_valida", {}, "00000000-0000-4000-8000-000000000000")
comprobar("R-04", "un token inventado no es una sesión", r[1] is False, r)

# ── Con sesión: lo que sí debe funcionar ──────────────────────────────────────
r = pedir("GET", "equipments?select=id", token=TE)
comprobar("R-05", "el estudiante ve el catálogo", len(filas(r)) > 0, f"{len(filas(r))} equipos")
r = pedir("GET", "loans?select=user_id", token=TE)
comprobar("R-05", "el estudiante solo ve sus préstamos",
          all(f["user_id"] == ID_ESTUDIANTE for f in filas(r)), f"{len(filas(r))} préstamos")
r = pedir("GET", "loans?select=status,solicitante:users!loans_user_id_fkey(full_name)&limit=1", token=TI)
comprobar("R-03", "con sesión se lee el nombre del solicitante", len(filas(r)) == 1, r)

# ── Ataques: deben ser rechazados ─────────────────────────────────────────────
r = pedir("POST", "loans", {
    "id": "a77ac000-0000-4000-8000-000000000001", "user_id": ID_INSTRUCTOR, "user_role": "ESTUDIANTE",
    "equipment_id": "0b1e0000-0000-4000-8000-000000000003", "status": "SOLICITADA",
    "request_date": "2026-09-25T10:00:00Z", "return_date": "2026-09-25T12:00:00Z",
    "environment": "x", "purpose": "ataque", "duration_hours": 2}, TE)
comprobar("R-05", "estudiante no crea préstamos a nombre de otro", r[0] in (401, 403), r)
r = pedir("PATCH", f"loans?id=eq.{PRESTAMO_DEMO}", {"status": "PRESTADO"}, TE)
comprobar("R-05", "estudiante no se aprueba su propia solicitud", r[0] == 400, r)
r = pedir("PATCH", f"loans?id=eq.{PRESTAMO_DEMO}", {"reviewed_by": ID_ESTUDIANTE}, TE)
comprobar("R-05", "estudiante no escribe la revisión del instructor", r[0] == 400, r)
r = pedir("PATCH", "equipments?id=eq.0b1e0000-0000-4000-8000-000000000003", {"name": "hackeado"}, TE)
comprobar("R-05", "estudiante no renombra equipos", r[0] == 400, r)
r = pedir("POST", "activities", {
    "id": "a77ac000-0000-4000-8000-000000000002", "title": "ataque", "description": "", "location": "x",
    "scheduled_at": "2030-01-01T10:00:00Z", "instructor_id": ID_INSTRUCTOR}, TE)
comprobar("R-05", "estudiante no crea actividades", r[0] in (401, 403), r)
r = pedir("PATCH", f"loans?id=eq.{PRESTAMO_DEMO_2}", {"status": "DEVUELTO"})
comprobar("R-05", "sin sesión no se modifica ningún préstamo", filas(r) == [], r)

# Borrado: sobre un equipo de prueba creado por el instructor
creado = pedir("POST", "equipments", {"id": EQUIPO_PRUEBA, "title": "Equipo de prueba de seguridad",
                                      "name": "Equipo de prueba de seguridad", "category": "Prueba",
                                      "status": "DISPONIBLE"}, TI)
comprobar("R-05", "el instructor sí crea equipos", creado[0] == 201, creado)
pedir("DELETE", f"equipments?id=eq.{EQUIPO_PRUEBA}", token=TE)
sigue = filas(pedir("GET", f"equipments?select=id&id=eq.{EQUIPO_PRUEBA}", token=TI))
comprobar("R-05", "estudiante no borra equipos", len(sigue) == 1, f"{len(sigue)} fila(s) tras el intento")
borrado = pedir("DELETE", f"equipments?id=eq.{EQUIPO_PRUEBA}", token=TI)
comprobar("R-05", "el instructor sí borra un equipo sin préstamos", len(filas(borrado)) == 1, borrado)

# ── Cierre de sesión ──────────────────────────────────────────────────────────
for token in (TE, TI, por_documento[0]["token"]):
    pedir("POST", "rpc/cerrar_sesion", {}, token)
r = pedir("POST", "rpc/sesion_valida", {}, TE)
comprobar("R-04", "tras cerrar sesión el token deja de servir", r[1] is False, r)

fallas = [x for x in resultados if not x[2]]
print(f"\n{len(resultados) - len(fallas)} de {len(resultados)} comprobaciones superadas.")
for riesgo, descripcion, _ in fallas:
    print(f"  FALLA {riesgo}: {descripcion}")
