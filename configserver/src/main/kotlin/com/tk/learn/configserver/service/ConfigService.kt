package com.tk.learn.configserver.service

import com.tk.learn.domain.models.*
import com.tk.learn.domain.dto.*
import com.tk.learn.configserver.repository.*
import kotlinx.datetime.Instant as KxInstant
import kotlin.time.Instant as KtInstant
import kotlin.time.ExperimentalTime

/**
 * Convert kotlin.time.Instant to kotlinx.datetime.Instant
 */
@OptIn(ExperimentalTime::class)
private fun KtInstant.toKxInstant(): KxInstant {
    return KxInstant.fromEpochMilliseconds(this.toEpochMilliseconds())
}

/**
 * Convert kotlinx.datetime.Instant to kotlin.time.Instant
 */
@OptIn(ExperimentalTime::class)
private fun KxInstant.toKtInstant(): KtInstant {
    return KtInstant.fromEpochMilliseconds(this.toEpochMilliseconds())
}

/**
 * Business logic for configuration management
 */
interface ConfigService {
    fun getPropertiesByAppAndDomain(applicationName: String, domain: String): PropertiesWithSyncResponse
    fun getPropertyYaml(applicationName: String, domain: String): String
    fun getConfigSyncInfo(applicationName: String, domain: String): ConfigSyncResponse
    fun getMetadata(): MetaResponse
    fun addProperty(
        applicationName: String,
        domain: String,
        request: PropertyCreateRequest
    ): AppConfigResponse
    fun updateProperties(
        applicationName: String,
        domain: String,
        request: PropertyUpdateRequest
    ): PropertiesWithSyncResponse
    fun getAuditHistory(applicationName: String, domain: String, limit: Int = 100): List<AuditHistoryResponse>
}

/**
 * Implementation of ConfigService
 */
class ConfigServiceImpl(
    private val appConfigRepository: AppConfigRepository,
    private val configSyncRepository: ConfigSyncRepository,
    private val auditRepository: AppConfigAuditRepository
) : ConfigService {

    @OptIn(ExperimentalTime::class)
    override fun getPropertiesByAppAndDomain(
        applicationName: String,
        domain: String
    ): PropertiesWithSyncResponse {
        val configs = appConfigRepository.findByAppAndDomain(applicationName, domain)
        val syncInfo = getConfigSyncInfo(applicationName, domain)

        val properties = configs.map { config ->
            AppConfigResponse(
                applicationName = config.applicationName,
                domain = config.domain,
                propertyKey = config.propertyKey,
                propertyValue = config.propertyValue,
                createdBy = config.createdBy,
                createdTm = config.createdTm.toKxInstant(),
                updatedBy = config.updatedBy,
                updatedTm = config.updatedTm.toKxInstant()
            )
        }

        return PropertiesWithSyncResponse(properties, syncInfo)
    }

    override fun getPropertyYaml(applicationName: String, domain: String): String {
        val configs = appConfigRepository.findByAppAndDomain(applicationName, domain)
        val syncInfo = getConfigSyncInfo(applicationName, domain)

        val yaml = StringBuilder()
        yaml.append("# Configuration for $applicationName in domain $domain\n")
        yaml.append("# Version: ${syncInfo.versionNumber}\n")
        yaml.append("# Last updated: ${syncInfo.updatedTm}\n")
        yaml.append("# Updated by: ${syncInfo.updatedBy}\n")
        yaml.append("\n")

        configs.forEach { config ->
            yaml.append("${config.propertyKey}: \"${config.propertyValue.replace("\"", "\\\"")}\"\n")
        }

        return yaml.toString()
    }

    @OptIn(ExperimentalTime::class)
    override fun getConfigSyncInfo(applicationName: String, domain: String): ConfigSyncResponse {
        val sync = configSyncRepository.findByAppAndDomain(applicationName, domain)
            ?: throw IllegalArgumentException("Configuration not found for $applicationName in $domain")

        return ConfigSyncResponse(
            applicationName = sync.applicationName,
            domain = sync.domain,
            versionNumber = sync.versionNumber,
            updatedBy = sync.updatedBy,
            updatedTm = sync.updatedTm.toKxInstant()
        )
    }

    override fun getMetadata(): MetaResponse {
        val domains = appConfigRepository.getDistinctDomains()
        val applicationsByDomain = domains.associate { domain ->
            domain to appConfigRepository.getApplicationsByDomain(domain)
        }

        return MetaResponse(
            domains = domains,
            applicationsByDomain = applicationsByDomain
        )
    }

    @OptIn(ExperimentalTime::class)
    override fun addProperty(
        applicationName: String,
        domain: String,
        request: PropertyCreateRequest
    ): AppConfigResponse {
        // Check if property already exists
        val existing = appConfigRepository.findByAppDomainAndKey(
            applicationName,
            domain,
            request.propertyKey
        )

        if (existing != null) {
            throw IllegalArgumentException("Property ${request.propertyKey} already exists for $applicationName in $domain")
        }

        // Get current sync info
        val syncInfo = configSyncRepository.findByAppAndDomain(applicationName, domain)
            ?: throw IllegalArgumentException("Configuration not found for $applicationName in $domain")

        val now = kotlinx.datetime.Clock.System.now().toKtInstant()

        // Insert new property
        val newConfig = AppConfig(
            applicationName = applicationName,
            domain = domain,
            propertyKey = request.propertyKey,
            propertyValue = request.propertyValue,
            createdBy = request.createdBy,
            createdTm = now,
            updatedBy = request.createdBy,
            updatedTm = now
        )

        appConfigRepository.insert(newConfig)

        // Create audit record
        auditRepository.insert(
            AppConfigAudit(
                applicationName = applicationName,
                domain = domain,
                propertyKey = request.propertyKey,
                oldPropertyValue = null,
                newPropertyValue = request.propertyValue,
                operation = AuditOperation.ADDED,
                updatedBy = request.createdBy,
                updatedTm = now,
                versionNumber = syncInfo.versionNumber + 1
            )
        )

        // Increment version
        configSyncRepository.incrementVersion(applicationName, domain, request.createdBy)

        return AppConfigResponse(
            applicationName = newConfig.applicationName,
            domain = newConfig.domain,
            propertyKey = newConfig.propertyKey,
            propertyValue = newConfig.propertyValue,
            createdBy = newConfig.createdBy,
            createdTm = newConfig.createdTm.toKxInstant(),
            updatedBy = newConfig.updatedBy,
            updatedTm = newConfig.updatedTm.toKxInstant()
        )
    }

    @OptIn(ExperimentalTime::class)
    override fun updateProperties(
        applicationName: String,
        domain: String,
        request: PropertyUpdateRequest
    ): PropertiesWithSyncResponse {
        // Get current sync info for version check
        val currentSync = configSyncRepository.findByAppAndDomain(applicationName, domain)
            ?: throw IllegalArgumentException("Configuration not found for $applicationName in $domain")

        // Check for concurrent update
        if (currentSync.versionNumber != request.expectedVersionNumber ||
            currentSync.updatedTm != request.expectedUpdatedTm.toKtInstant()) {
            throw IllegalStateException("Configuration has been modified. Please refresh and try again.")
        }

        val now = kotlinx.datetime.Clock.System.now().toKtInstant()

        // Update each property and create audit records
        request.properties.forEach { (key, newValue) ->
            val existing = appConfigRepository.findByAppDomainAndKey(applicationName, domain, key)

            if (existing != null) {
                val oldValue = existing.propertyValue

                // Update property
                appConfigRepository.update(
                    existing.copy(
                        propertyValue = newValue,
                        updatedBy = request.updatedBy,
                        updatedTm = now
                    )
                )

                // Create audit record
                auditRepository.insert(
                    AppConfigAudit(
                        applicationName = applicationName,
                        domain = domain,
                        propertyKey = key,
                        oldPropertyValue = oldValue,
                        newPropertyValue = newValue,
                        operation = AuditOperation.MODIFIED,
                        updatedBy = request.updatedBy,
                        updatedTm = now,
                        versionNumber = currentSync.versionNumber + 1
                    )
                )
            }
        }

        // Increment version
        configSyncRepository.incrementVersion(applicationName, domain, request.updatedBy)

        // Return updated properties with new sync info
        return getPropertiesByAppAndDomain(applicationName, domain)
    }

    @OptIn(ExperimentalTime::class)
    override fun getAuditHistory(
        applicationName: String,
        domain: String,
        limit: Int
    ): List<AuditHistoryResponse> {
        return auditRepository.findByAppAndDomain(applicationName, domain, limit).map {
            AuditHistoryResponse(
                id = it.id ?: 0L,
                applicationName = it.applicationName,
                domain = it.domain,
                propertyKey = it.propertyKey,
                oldPropertyValue = it.oldPropertyValue,
                newPropertyValue = it.newPropertyValue,
                operation = it.operation,
                updatedBy = it.updatedBy,
                updatedTm = it.updatedTm.toKxInstant(),
                versionNumber = it.versionNumber
            )
        }
    }
}

