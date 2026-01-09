package com.tk.learn.domain.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import com.tk.learn.domain.models.AuditOperation

/**
 * Response DTO for configuration properties
 */
@Serializable
data class AppConfigResponse(
    val applicationName: String,
    val domain: String,
    val propertyKey: String,
    val propertyValue: String,
    val createdBy: String,
    val createdTm: Instant,
    val updatedBy: String,
    val updatedTm: Instant
)

/**
 * Response DTO for configuration sync info
 */
@Serializable
data class ConfigSyncResponse(
    val applicationName: String,
    val domain: String,
    val versionNumber: Int,
    val updatedBy: String,
    val updatedTm: Instant
)

/**
 * Request DTO for updating properties
 */
@Serializable
data class PropertyUpdateRequest(
    val properties: Map<String, String>,
    val expectedVersionNumber: Int,
    val expectedUpdatedTm: Instant,
    val updatedBy: String
)

/**
 * Request DTO for adding a new property
 */
@Serializable
data class PropertyCreateRequest(
    val propertyKey: String,
    val propertyValue: String,
    val createdBy: String
)

/**
 * Response DTO for metadata (domains and applications)
 */
@Serializable
data class MetaResponse(
    val domains: List<String>,
    val applicationsByDomain: Map<String, List<String>>
)

/**
 * Response DTO for properties with sync info
 */
@Serializable
data class PropertiesWithSyncResponse(
    val properties: List<AppConfigResponse>,
    val syncInfo: ConfigSyncResponse
)

/**
 * Response DTO for audit history
 */
@Serializable
data class AuditHistoryResponse(
    val id: Long,
    val applicationName: String,
    val domain: String,
    val propertyKey: String,
    val oldPropertyValue: String?,
    val newPropertyValue: String?,
    val operation: AuditOperation,
    val updatedBy: String,
    val updatedTm: Instant,
    val versionNumber: Int
)

