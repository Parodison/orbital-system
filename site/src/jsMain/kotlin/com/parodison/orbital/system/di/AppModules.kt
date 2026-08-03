package com.parodison.orbital.system.di

import com.parodison.orbital.system.controllers.GeolocationController
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.shared.BuildKonfig
import com.varabyte.kobweb.core.init.InitKobweb
import com.varabyte.kobweb.core.init.InitKobwebContext
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.cbor.cbor
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { SatelliteTrackerController(get(), get()) }
    single { GeolocationController() }

}

@OptIn(ExperimentalSerializationApi::class)
val networkModule = module {
    single {
        HttpClient {
            defaultRequest {
                url(BuildKonfig.BACKEND_URL)
                accept(ContentType.Application.Cbor)
            }
            install(Resources)
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
                cbor(Cbor {
                    ignoreUnknownKeys = true
                })

            }
        }
    }
}

@InitKobweb
fun initKoin(ctx: InitKobwebContext) {
    startKoin {
        modules(appModule, networkModule)
    }
}