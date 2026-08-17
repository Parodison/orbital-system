import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.Properties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildKonfig)
}

kotlin {

    iosArm64()
    iosSimulatorArm64()

    jvm()
    jvmToolchain(21)

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.serialization.kotlinx.cbor)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.resources)
            implementation(libs.orbit.core)
            implementation(libs.kermit)
        }
    }
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

buildkonfig {
    packageName = "com.parodison.shared"

    exposeObjectWithName = "BuildKonfig"

    defaultConfigs {
        buildConfigField(
            type = STRING,
            name = "BACKEND_URL",
            value = localProps.getProperty("backend.url"),
        )
    }
}
