package com.tk.learn.domain.models

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents the sync status and version of configuration for an application
 */
data class ConfigSync @OptIn(ExperimentalTime::class) constructor(
    val id: Long? = null,
    val applicationName: String,
    val domain: String,
    val versionNumber: Int,
    val updatedBy: String,
    val updatedTm: Instant
)

