package com.tk.learn.configserver.controller

import io.javalin.http.Context
import io.javalin.http.HttpStatus
import com.tk.learn.configserver.service.ConfigService
import com.tk.learn.domain.dto.*
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * REST Controller for configuration management endpoints
 */
class ConfigController(
    private val configService: ConfigService,
    private val objectMapper: ObjectMapper
) {

    /**
     * GET /config/yml/{domain}/{application}
     * Returns configuration as YAML format
     */
    fun getConfigYaml(ctx: Context) {
        try {
            val domain = ctx.pathParam("domain")
            val application = ctx.pathParam("application")

            if (domain.isBlank() || application.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                ctx.json(mapOf("error" to "Domain and application are required"))
                return
            }

            val yaml = configService.getPropertyYaml(application, domain)
            ctx.contentType("application/x-yaml")
            ctx.result(yaml)
            ctx.status(HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            ctx.status(HttpStatus.NOT_FOUND)
            ctx.json(mapOf("error" to e.message))
        } catch (e: Exception) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            ctx.json(mapOf("error" to "Failed to retrieve configuration: ${e.message}"))
        }
    }

    /**
     * GET /config/sync/{domain}/{application}
     * Returns sync metadata (version, timestamp) for polling
     */
    fun getConfigSync(ctx: Context) {
        try {
            val domain = ctx.pathParam("domain")
            val application = ctx.pathParam("application")

            if (domain.isBlank() || application.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                ctx.json(mapOf("error" to "Domain and application are required"))
                return
            }

            val syncInfo = configService.getConfigSyncInfo(application, domain)
            ctx.json(syncInfo)
            ctx.status(HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            ctx.status(HttpStatus.NOT_FOUND)
            ctx.json(mapOf("error" to e.message))
        } catch (e: Exception) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            ctx.json(mapOf("error" to "Failed to retrieve sync info: ${e.message}"))
        }
    }

    /**
     * GET /config/meta
     * Returns metadata about available domains and applications
     */
    fun getMetadata(ctx: Context) {
        try {
            val metadata = configService.getMetadata()
            ctx.json(metadata)
            ctx.status(HttpStatus.OK)
        } catch (e: Exception) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            ctx.json(mapOf("error" to "Failed to retrieve metadata: ${e.message}"))
        }
    }

    /**
     * GET /config/properties/{domain}/{application}
     * Returns all properties as JSON with sync info
     */
    fun getProperties(ctx: Context) {
        try {
            val domain = ctx.pathParam("domain")
            val application = ctx.pathParam("application")

            if (domain.isBlank() || application.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                ctx.json(mapOf("error" to "Domain and application are required"))
                return
            }

            val response = configService.getPropertiesByAppAndDomain(application, domain)
            ctx.json(response)
            ctx.status(HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            ctx.status(HttpStatus.NOT_FOUND)
            ctx.json(mapOf("error" to e.message))
        } catch (e: Exception) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            ctx.json(mapOf("error" to "Failed to retrieve properties: ${e.message}"))
        }
    }

    /**
     * PUT /config/properties/{domain}/{application}
     * Bulk update properties with optimistic locking
     */
    fun updateProperties(ctx: Context) {
        try {
            val domain = ctx.pathParam("domain")
            val application = ctx.pathParam("application")

            if (domain.isBlank() || application.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                ctx.json(mapOf("error" to "Domain and application are required"))
                return
            }

            val request = ctx.bodyAsClass(PropertyUpdateRequest::class.java)

            if (request.properties.isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                ctx.json(mapOf("error" to "Properties map cannot be empty"))
                return
            }

            val response = configService.updateProperties(application, domain, request)
            ctx.json(response)
            ctx.status(HttpStatus.OK)
        } catch (e: IllegalStateException) {
            ctx.status(HttpStatus.CONFLICT)
            ctx.json(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ctx.status(HttpStatus.BAD_REQUEST)
            ctx.json(mapOf("error" to e.message))
        } catch (e: Exception) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            ctx.json(mapOf("error" to "Failed to update properties: ${e.message}"))
        }
    }

    /**
     * POST /config/properties/{domain}/{application}
     * Add a new property
     */
    fun addProperty(ctx: Context) {
        try {
            val domain = ctx.pathParam("domain")
            val application = ctx.pathParam("application")

            if (domain.isBlank() || application.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                ctx.json(mapOf("error" to "Domain and application are required"))
                return
            }

            val request = ctx.bodyAsClass(PropertyCreateRequest::class.java)

            if (request.propertyKey.isBlank() || request.propertyValue.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                ctx.json(mapOf("error" to "Property key and value are required"))
                return
            }

            val response = configService.addProperty(application, domain, request)
            ctx.json(response)
            ctx.status(HttpStatus.CREATED)
        } catch (e: IllegalArgumentException) {
            ctx.status(HttpStatus.BAD_REQUEST)
            ctx.json(mapOf("error" to e.message))
        } catch (e: Exception) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            ctx.json(mapOf("error" to "Failed to add property: ${e.message}"))
        }
    }

    /**
     * GET /config/audit/{domain}/{application}
     * Returns audit history
     */
    fun getAuditHistory(ctx: Context) {
        try {
            val domain = ctx.pathParam("domain")
            val application = ctx.pathParam("application")
            val limit = ctx.queryParam("limit")?.toIntOrNull() ?: 100

            if (domain.isBlank() || application.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                ctx.json(mapOf("error" to "Domain and application are required"))
                return
            }

            val history = configService.getAuditHistory(application, domain, limit)
            ctx.json(history)
            ctx.status(HttpStatus.OK)
        } catch (e: Exception) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            ctx.json(mapOf("error" to "Failed to retrieve audit history: ${e.message}"))
        }
    }
}

