package com.parodison.orbital.server.plugin

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabases() {
    val dataSource = createHikariDataSource(environment.config.config("database"))

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .load()
        .migrate()

    Database.connect(dataSource)
}

private fun createHikariDataSource(config: ApplicationConfig): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        driverClassName = config.property("driver").getString()
        jdbcUrl = config.property("url").getString()
        username = config.property("user").getString()
        password = config.property("password").getString()
        maximumPoolSize = config.property("maxPoolSize").getString().toInt()
        isAutoCommit = false
        validate()
    }
    return HikariDataSource(hikariConfig)
}
