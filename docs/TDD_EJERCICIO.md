# Ejercicio de Desarrollo Guiado por Pruebas (TDD) - PréstamoLab CTMA

Este documento evidencia el desarrollo de la regla de validación de credenciales de Administrador siguiendo el ciclo **TDD (Test-Driven Development)**: **RED -> GREEN -> REFACTOR**.

---

## Módulo Desarrollado: `AdminAuthValidator`

El objetivo fue construir un validador aislado de formato de correo, longitud de contraseña y autenticación de credenciales de administración.

---

## Ciclo de Desarrollo TDD

### Fase 1: RED (Escribir Pruebas que Fallan)
En primer lugar, se escribieron las pruebas unitarias en `AdminAuthValidatorTest.kt` definiendo el comportamiento esperado antes de escribir el código de producción:

```kotlin
@Test
fun emailInvalido_retornaFalse() {
    assertFalse(AdminAuthValidator.isValidEmail("admin"))
    assertFalse(AdminAuthValidator.isValidEmail(""))
}

@Test
fun credencialesCorrectas_autenticaAdmin() {
    assertTrue(AdminAuthValidator.validateAdminCredentials("admin@gmail.com", "admin123"))
}
```

*Resultado inicial:* La compilación falló (RED) porque la clase `AdminAuthValidator` no existía.

---

### Fase 2: GREEN (Escribir el Código Mínimo Necesario)
Se creó la implementación básica en `AdminAuthValidator.kt` para hacer pasar las pruebas unitarias:

```kotlin
object AdminAuthValidator {
    fun isValidEmail(email: String): Boolean = email.contains("@") && email.contains(".")
    fun isValidPassword(password: String): Boolean = password.length >= 6
    fun validateAdminCredentials(email: String, password: String): Boolean {
        return email == "admin@gmail.com" && password == "admin123"
    }
}
```

*Resultado:* Todas las pruebas pasaron a verde (GREEN).

---

### Fase 3: REFACTOR (Refactorizar y Optimizar)
Se mejoró la precisión del formateo mediante expresiones regulares (Regex), limpieza de espacios (`trim()`) e insensibilidad a mayúsculas/minúsculas para el correo:

```kotlin
object AdminAuthValidator {

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
        return emailRegex.matches(email.trim())
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun validateAdminCredentials(email: String, password: String): Boolean {
        return isValidEmail(email) &&
                email.trim().equals("admin@gmail.com", ignoreCase = true) &&
                password == "admin123"
    }
}
```

*Resultado:* El código refactorizado mantiene el 100% de las pruebas pasando (GREEN) con mayor solidez.

---

## Verificación

Para ejecutar la prueba unitaria creada por TDD:
```bash
./gradlew testDebugUnitTest --tests "com.example.prestamolab.util.AdminAuthValidatorTest"
```
