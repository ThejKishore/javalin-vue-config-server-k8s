package com.tk.learn.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.tk.learn.configserver.config.configServerModule
import com.tk.learn.configserver.controller.ConfigController
import io.javalin.Javalin
import io.javalin.http.HttpStatus
import io.javalin.json.JsonMapper
import org.jdbi.v3.core.Jdbi
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Type

fun main() {
    // Initialize Koin DI
    startKoin {
        modules(configServerModule())
    }

    // Get instances from Koin
    val jdbi: Jdbi by inject(Jdbi::class.java)
    val configController: ConfigController by inject(ConfigController::class.java)
    val objectMapper: ObjectMapper by inject(ObjectMapper::class.java)

    // Initialize database schema
    initializeDatabase(jdbi)

    // Create and configure Javalin app
    val app = Javalin.create { config ->
        config.jsonMapper(createJacksonMapper(objectMapper))
        config.staticFiles.add { staticFileConfig ->
            staticFileConfig.hostedPath = "/admin/ui"
            staticFileConfig.directory = "/public"
        }
    }.start(7070)

    // Configure routes
    configureRoutes(app, configController)

    // Basic health check
    app.get("/health") { ctx ->
        ctx.status(HttpStatus.OK)
        ctx.json(mapOf("status" to "UP"))
    }

    println("Platform Management Server started on http://localhost:7070")
}

/**
 * Initialize the H2 database schema
 */
private fun initializeDatabase(jdbi: Jdbi) {
    try {
        val schemaSQL = readResourceFile("/schema.sql")
        jdbi.withHandle<Void, Exception> { handle ->
            schemaSQL.split(";").forEach { statement ->
                val trimmed = statement.trim()
                if (trimmed.isNotEmpty()) {
                    handle.execute(trimmed)
                }
            }
            null
        }
        println("Database schema initialized successfully")
    } catch (e: Exception) {
        println("Error initializing database: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Read a resource file from classpath
 */
private fun readResourceFile(resourcePath: String): String {
    val inputStream = object {}.javaClass.getResourceAsStream(resourcePath)
        ?: throw IllegalStateException("Resource not found: $resourcePath")

    return BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
}

/**
 * Create Jackson JSON mapper for Javalin
 */
private fun createJacksonMapper(objectMapper: ObjectMapper): JsonMapper {
    return object : JsonMapper {
        override fun <T : Any> fromJsonString(json: String, targetType: Type): T {
            return objectMapper.readValue(json, objectMapper.typeFactory.constructType(targetType))
        }

        override fun toJsonString(obj: Any, type: Type): String {
            return objectMapper.writeValueAsString(obj)
        }
    }
}

private const val API_CONFIG = "/api/config"

/**
 * Configure all REST API routes
 */
private fun configureRoutes(app: Javalin, configController: ConfigController) {
    // Configuration endpoints
    app.get("$API_CONFIG/yml/{domain}/{application}", configController::getConfigYaml)
    app.get("$API_CONFIG/sync/{domain}/{application}", configController::getConfigSync)
    app.get("$API_CONFIG/meta", configController::getMetadata)
    app.get("$API_CONFIG/properties/{domain}/{application}", configController::getProperties)
    app.put("$API_CONFIG/properties/{domain}/{application}", configController::updateProperties)
    app.post("$API_CONFIG/properties/{domain}/{application}", configController::addProperty)
    app.delete("$API_CONFIG/properties/{domain}/{application}/{propertyKey}", configController::deleteProperty)
    app.post("$API_CONFIG/onboard", configController::onboardService)
    app.get("$API_CONFIG/audit/{domain}/{application}", configController::getAuditHistory)
}

