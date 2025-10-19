package com.example.models

import org.jetbrains.exposed.dao.id.IntIdTable


object UserTable : IntIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 255).nullable()
    val passwordHash = varchar("password", 60)
}