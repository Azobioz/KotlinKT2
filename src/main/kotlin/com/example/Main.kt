package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import com.example.db.DatabaseFactory
import com.example.routes.registerUserRoutes
import com.example.auth.JwtConfig
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.response.respond


fun main() {
    val config = HoconApplicationConfig(ConfigFactory.load())
    embeddedServer(
        Netty,
        environment = applicationEngineEnvironment {
            this.config = config
            connector {
                port = config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt() ?: 8085
            }
            module(Application::module)
        }
    ).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }

    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/kotlin_kt2_db"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPass = System.getenv("DB_PASSWORD") ?: "12345"
    val dbDriver = System.getenv("DB_DRIVER") ?: "org.postgresql.Driver"

    DatabaseFactory.init(dbUrl, dbUser, dbPass, dbDriver)

    val jwtSecret = System.getenv("JWT_SECRET") ?: "123456"
    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "some-user"
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "user-audience"
    val jwtRealm = System.getenv("JWT_REALM") ?: "ktor-sample-realm"
    val jwtTtl = System.getenv("JWT_TTL")?.toLong() ?: 3600

    JwtConfig.initialize(jwtSecret, jwtIssuer, jwtAudience, jwtRealm, jwtTtl)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(JwtConfig.verifier)
            validate { credential ->
                val username = credential.payload.getClaim("username").asString()
                if (username != null) JWTPrincipal(credential.payload) else null
            }
        }
    }

    routing {
        registerUserRoutes()
    }
}
