package com.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.example.models.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


object DatabaseFactory {
    fun init(url: String, user: String, password: String, driver: String) {

            val config = HikariConfig().apply {
                jdbcUrl = url
                username = user
                this.password = password
                this.driverClassName = driver
                maximumPoolSize = 5
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                connectionTimeout = 5000
            }

            val ds = HikariDataSource(config)

            Database.connect(ds)

            transaction {
                SchemaUtils.create(UserTable)
            }

    }
}