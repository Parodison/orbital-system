import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.html.link
import kotlinx.html.style
import kotlinx.html.unsafe

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

group = "com.parodison.orbital.system"
version = "1.0"

kobweb {
    app {
        index {
            description.set("Orbital System")
            head.add {
                link(rel = "preconnect", href = "https://fonts.googleapis.com")
                link(rel = "preconnect", href = "https://fonts.gstatic.com") { attributes["crossorigin"] = "" }
                link(href="https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap", rel = "stylesheet")
                link {
                    href = "https://unpkg.com/maplibre-gl@5.0.1/dist/maplibre-gl.css"
                    rel = "stylesheet"
                }
                link {
                    href = "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.css"
                    rel = "stylesheet"
                }
            }
        }
    }
}

kotlin {
    // This example is frontend only. However, for a fullstack app, you can uncomment the includeServer parameter
    // and the `jvmMain` source set below.
    configAsKobwebApplication("system" /*, includeServer = true*/)

    js {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.serialization.kotlinx.cbor)
            implementation(libs.kotlinx.datetime)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.orbit.core)
            implementation(project(":shared"))
            implementation(libs.ktor.resources)
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.client.websockets)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.silk)
            // This default template uses built-in SVG icons, but what's available is limited.
            // Uncomment the following if you want access to a large set of font-awesome icons:
            // implementation(libs.silk.icons.fa)
            implementation(libs.kobwebx.markdown)
            implementation(libs.silk.icons.mdi)
            implementation(libs.silk.icons.fa)
            implementation(libs.kobwebx.serialization.kotlinx)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.html.material3)
            implementation(npm("maplibre-gl", "5.24.0"))
            implementation(npm("bootstrap-icons", "1.13.1"))
        }

        // Uncomment the following if you pass `includeServer = true` into the `configAsKobwebApplication` call.
//        jvmMain.dependencies {
//            compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
//        }
    }
}