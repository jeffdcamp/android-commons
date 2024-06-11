package org.dbtools.android.commons.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.dbtools.android.commons.log.KtorKermitLogger
import org.dbtools.android.commons.network.ktor.ResponseTimePlugin

object TestHttpClientProvider {
    fun getTestClient(
        engine: HttpClientEngine,
    ): HttpClient {
        return HttpClient(engine) {
            install(ResponseTimePlugin)
            install(Logging) {
                logger = KtorKermitLogger
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        useArrayPolymorphism = true
                        allowSpecialFloatingPointValues = true
                        explicitNulls = false
                    }
                )
            }
            install(Resources)
        }
    }
}