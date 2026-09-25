# Matriz de Trazabilidad de Requisitos - PrestamoLab CTMA

## Matriz de Requisitos vs Pruebas y Componentes (Semana 9)

| Codigo HU / Req | Descripción | Componentes UI / Data | Pruebas Unitarias / UI | Estado |
|---|---|---|---|---|
| **HU-01** | Catálogo de Equipos y Estado | `CatalogoScreen.kt`, `EquipmentEntity.kt`, `PrestamoDao.kt` | `InMemoryPrestamoRepositoryTest` | ✅ Implementado |
| **HU-02** | Solicitud de Préstamos con Validaciones | `SolicitarEquipoScreen.kt`, `PrestamoViewModel.kt` | `ValidacionesSolicitudTest`, `PrestamoViewModelTest` | ✅ Implementado |
| **HU-03** | Cancelación y Liberación de Equipos | `SolicitudDetalleScreen.kt`, `RoomPrestamoRepository.kt` | `PrestamoViewModelTest` | ✅ Implementado |
| **HU-04** | Autenticación Segura y DataStore | `LoginScreen.kt`, `AuthViewModel.kt`, `UserSessionManager.kt` | `LoginAndNavigationTest` | ✅ Implementado |
| **HU-05** | Control de Acceso por Roles (Admin vs Usuario) | `NavGraph.kt`, `AdminDashboardScreen.kt` | `RolePermissionsTest` | ✅ Implementado |
| **HU-06** | CRUD de Inventario Administrativo | `AdminDashboardScreen.kt`, `FormularioEquipoScreen.kt` | `RolePermissionsTest` | ✅ Implementado |
| **HU-07** | Evidencias Fotográficas (Cámara & Supabase Storage) | `CapturaEvidenciaScreen.kt`, `SupabaseClient.kt` | Pruebas de compilación y FileProvider | ✅ Implementado |
