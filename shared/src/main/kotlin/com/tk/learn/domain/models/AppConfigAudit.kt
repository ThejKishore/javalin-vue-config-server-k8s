package com.tk.learn.domain.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

/**
 * Operation type for audit trail
 */
@Serializable
enum class AuditOperation {
    ADDED,
    MODIFIED,
    DELETED
}

/**
 * Audit trail for configuration changes
 */
data class AppConfigAudit @OptIn(ExperimentalTime::class) constructor(
    val id: Long? = null,
    val applicationName: String,
    val domain: String,
    val propertyKey: String,
    val oldPropertyValue: String? = null,
    val newPropertyValue: String? = null,
    val operation: AuditOperation,
    val updatedBy: String,
    val updatedTm: Instant,
    val versionNumber: Int
)

