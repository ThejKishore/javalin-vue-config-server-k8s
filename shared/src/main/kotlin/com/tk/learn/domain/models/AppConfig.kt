package com.tk.learn.domain.models

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents an application configuration property
 */
data class AppConfig @OptIn(ExperimentalTime::class) constructor(
    val id: Long? = null,
    val applicationName: String,
    val domain: String,
    val propertyKey: String,
    val propertyValue: String,
    val createdBy: String,
    val createdTm: Instant,
    val updatedBy: String,
    val updatedTm: Instant
)

