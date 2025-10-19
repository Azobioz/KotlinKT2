package com.example.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.auth.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.models.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.registerUserRoutes() {
    route("/users") {
        post {
            val req = try {
                call.receive<UserCreateRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid json"))
                return@post
            }

            if (req.username.isBlank() || req.password.length < 6) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "username and password required; password >= 6")
                )
                return@post
            }

            val hashed = BCrypt.withDefaults().hashToString(12, req.password.toCharArray())

            try {
                val generatedId = transaction {
                    UserTable.insertAndGetId {
                        it[username] = req.username
                        it[email] = req.email
                        it[passwordHash] = hashed
                    }.value
                }
                call.respond(HttpStatusCode.Created, UserResponse(generatedId, req.username, req.email))
            } catch (e: ExposedSQLException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "username already exists"))
            }
        }

        authenticate("auth-jwt") {
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid id"))
                    return@put
                }

                val req = try {
                    call.receive<UserUpdateRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid json"))
                    return@put
                }

                val principal = call.principal<JWTPrincipal>()
                val requesterUsername = principal?.getClaim("username", String::class)

                val targetUser = transaction {
                    UserTable.select { UserTable.id eq id }.singleOrNull()
                }

                if (targetUser == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "user not found"))
                    return@put
                }

                val targetUsername = targetUser[UserTable.username]
                if (requesterUsername != targetUsername) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "you can only update your own account"))
                    return@put
                }

                // Хэшируем пароль только если он передан
                val newHashedPassword = req.password?.let {
                    BCrypt.withDefaults().hashToString(12, it.toCharArray())
                }

                transaction {
                    UserTable.update({ UserTable.id eq id }) { row ->
                        req.username?.let { row[username] = it }
                        req.email?.let { row[email] = it }
                        newHashedPassword?.let { row[passwordHash] = it }
                    }
                }

                call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
            }
        }


        post("/login") {
            val req = try {
                call.receive<LoginRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid json"))
                return@post
            }

            val record = transaction {
                UserTable.select { UserTable.username eq req.username }.singleOrNull()
            }
            if (record == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid credentials"))
                return@post
            }
            val hash = record[UserTable.passwordHash]
            val result = BCrypt.verifyer().verify(req.password.toCharArray(), hash)
            if (!result.verified) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid credentials"))
                return@post
            }
            val token = JwtConfig.makeToken(req.username)
            call.respond(TokenResponse(token))
        }

        get {
            val usernameQuery = call.request.queryParameters["username"]
            val users = transaction {
                val query = if (!usernameQuery.isNullOrBlank()) {
                    UserTable.select { UserTable.username like "%${usernameQuery}%" }
                } else {
                    UserTable.selectAll()
                }
                query.map {
                    UserResponse(it[UserTable.id].value, it[UserTable.username], it[UserTable.email])
                }
            }
            call.respond(users)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid id"))
                return@get
            }
            val user = transaction {
                UserTable.select { UserTable.id eq id }.singleOrNull()
            }
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "user not found"))
                return@get
            }
            call.respond(UserResponse(user[UserTable.id].value, user[UserTable.username], user[UserTable.email]))
        }

        authenticate("auth-jwt") {
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid id"))
                    return@delete
                }

                val principal = call.principal<JWTPrincipal>()
                val requesterUsername = principal?.getClaim("username", String::class)

                if (requesterUsername == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "missing or invalid token"))
                    return@delete
                }

                val targetUser = transaction {
                    UserTable.select { UserTable.id eq id }.singleOrNull()
                }

                if (targetUser == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "user not found"))
                    return@delete
                }

                val targetUsername = targetUser[UserTable.username]
                if (requesterUsername != targetUsername) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "you can only delete your own account")
                    )
                    return@delete
                }

                val deleted = transaction {
                    UserTable.deleteWhere { UserTable.id eq id }
                }

                if (deleted == 0) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "delete failed"))
                    return@delete
                }

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("status" to "deleted", "user" to requesterUsername)
                )
            }
        }

    }
}
