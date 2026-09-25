import java.util.Properties

// Cargar propiedades locales desde local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.prestamolab"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.prestamolab"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.prestamolab.PrestamoLabTestRunner"

        // Ambientes dev/stage/prod (docs/AMBIENTES.md): se eligen con -Pambiente=... o la variable
        // PRESTAMOLAB_AMBIENTE; por defecto dev. Cada ambiente tiene su URL y su clave en
        // local.properties o en variables de entorno (secretos del CI), nunca en git.
        val ambiente = (findProperty("ambiente") as String? ?: System.getenv("PRESTAMOLAB_AMBIENTE") ?: "dev").lowercase()
        if (ambiente !in listOf("dev", "stage", "prod")) {
            throw GradleException("Ambiente '$ambiente' no válido: use dev, stage o prod")
        }
        fun credencial(nombre: String): String {
            val porAmbiente = "${nombre}_${ambiente.uppercase()}"
            return localProperties.getProperty(porAmbiente) ?: System.getenv(porAmbiente)
                // dev conserva las claves originales sin sufijo (SUPABASE_URL, SUPABASE_ANON_KEY)
                ?: (if (ambiente == "dev") localProperties.getProperty(nombre) ?: System.getenv(nombre) else null)
                ?: ""
        }
        val supabaseUrl = credencial("SUPABASE_URL")
        val supabaseKey = credencial("SUPABASE_ANON_KEY")

        // stage y prod no compilan sin credenciales ni con URL sin HTTPS: así nunca se publica
        // una versión que apunte en silencio a otro ambiente o que envíe datos en texto plano.
        // dev puede quedar vacío (CI sin secretos: las pruebas usan dobles y MockWebServer).
        if (ambiente != "dev" && (supabaseUrl.isBlank() || supabaseKey.isBlank())) {
            throw GradleException("Faltan SUPABASE_URL_${ambiente.uppercase()} o SUPABASE_ANON_KEY_${ambiente.uppercase()} para el ambiente $ambiente")
        }
        if (supabaseUrl.isNotBlank() && !supabaseUrl.startsWith("https://")) {
            throw GradleException("La URL de Supabase del ambiente $ambiente debe usar HTTPS")
        }

        buildConfigField("String", "AMBIENTE", "\"$ambiente\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets {
        // Esquemas exportados por Room: los usa MigrationTestHelper en las pruebas de migración
        getByName("androidTest").assets.directories.add("$projectDir/schemas")
    }
    buildFeatures {
        compose = true
        buildConfig = true // Habilitar la generación de BuildConfig
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // collectAsStateWithLifecycle: la UI deja de recolectar cuando no está visible
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.play.services.location)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.work.runtime.ktx)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.mockwebserver)
    // org.json real: en la JVM el android.jar solo trae stubs que lanzan excepción
    testImplementation(libs.org.json)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.work.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    constraints {
        // room-testing (MigrationTestHelper) exige 1.8.1; sin esto el APK de pruebas hereda la
        // 1.7.3 de room-runtime y falla con AbstractMethodError al leer los esquemas exportados
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")
    }
}