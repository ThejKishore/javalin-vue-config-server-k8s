package com.tk.learn.configserver.config

import org.koin.dsl.module
import org.jdbi.v3.core.Jdbi
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import kotlinx.datetime.Instant
import com.tk.learn.configserver.repository.*
import com.tk.learn.configserver.service.*
import com.tk.learn.configserver.controller.ConfigController
import javax.sql.DataSource

/**
 * Custom serializer for kotlinx.datetime.Instant
 */
class KotlinxInstantSerializer : StdScalarSerializer<Instant>(Instant::class.java) {
    override fun serialize(value: Instant, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

/**
 * Custom deserializer for kotlinx.datetime.Instant
 */
class KotlinxInstantDeserializer : JsonDeserializer<Instant>() {
    override fun deserialize(p: com.fasterxml.jackson.core.JsonParser, ctxt: DeserializationContext): Instant {
        return Instant.parse(p.text)
    }
}

/**
 * Koin module for dependency injection configuration
 */
fun configServerModule() = module {

    // DataSource with HikariCP
    single<DataSource> {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:h2:mem:configdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
            driverClassName = "org.h2.Driver"
            username = "sa"
            password = ""
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30000
        }
        HikariDataSource(config) as DataSource
    }

    // JDBI configuration
    single {
        val dataSource = get<DataSource>()
        Jdbi.create(dataSource).apply {
            registerRowMapper(AppConfigRowMapper())
            registerRowMapper(ConfigSyncRowMapper())
            registerRowMapper(AppConfigAuditRowMapper())
        }
    }

    // Jackson ObjectMapper
    single {
        ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            val module = com.fasterxml.jackson.databind.module.SimpleModule()
            module.addSerializer(Instant::class.java, KotlinxInstantSerializer())
            module.addDeserializer(Instant::class.java, KotlinxInstantDeserializer())
            registerModule(module)
        }
    }

    // Repositories
    single<AppConfigRepository> {
        AppConfigRepositoryImpl(get())
    }

    single<ConfigSyncRepository> {
        ConfigSyncRepositoryImpl(get())
    }

    single<AppConfigAuditRepository> {
        AppConfigAuditRepositoryImpl(get())
    }

    // Services
    single<ConfigService> {
        ConfigServiceImpl(get(), get(), get())
    }

    // Controllers
    single {
        ConfigController(get(), get())
    }
}

