package com.tk.learn.configserver.service

import com.tk.learn.domain.models.*
import com.tk.learn.domain.dto.*
import com.tk.learn.configserver.repository.*
import org.yaml.snakeyaml.Yaml
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
    fun deleteProperty(
        applicationName: String,
        domain: String,
        propertyKey: String
    ): PropertiesWithSyncResponse
    fun onboardService(
        domain: String,
        applicationName: String,
        file: io.javalin.http.UploadedFile
    ): PropertiesWithSyncResponse
    fun getAuditHistory(applicationName: String, domain: String, limit: Int = 100): List<AuditHistoryResponse>
}

/**
 * Implementation of ConfigService
 */
class ConfigServiceImpl(
    private val appConfigRepository: AppConfigRepository,
    private val configSyncRepository: ConfigSyncRepository,
    private val auditRepository: AppConfigAuditRepository,
    private val objectMapper: com.fasterxml.jackson.databind.ObjectMapper
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

        yaml.append(convertToYaml(configs))
        return yaml.toString()
    }

    private fun convertToYaml(props: List<AppConfig>): String {
        val map = mutableMapOf<String, Any>()
        props.forEach { prop ->
            val keys = prop.propertyKey.split(".")
            var current = map
            keys.forEachIndexed { i, k ->
                if (i == keys.lastIndex) {
                    current[k] = prop.propertyValue
                } else {
                    @Suppress("UNCHECKED_CAST")
                    current = current.getOrPut(k) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
                }
            }
        }

        val options = org.yaml.snakeyaml.DumperOptions()
        options.defaultFlowStyle = org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
        options.isPrettyFlow = true
        val yaml = Yaml(options)
        return yaml.dump(map)
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
    override fun deleteProperty(
        applicationName: String,
        domain: String,
        propertyKey: String
    ): PropertiesWithSyncResponse {
        // Get current sync info for version check
        val currentSync = configSyncRepository.findByAppAndDomain(applicationName, domain)
            ?: throw IllegalArgumentException("Configuration not found for $applicationName in $domain")

        // Check if property exists
        val existing = appConfigRepository.findByAppDomainAndKey(applicationName, domain, propertyKey)
            ?: throw IllegalArgumentException("Property $propertyKey not found for $applicationName in $domain")

        val now = kotlinx.datetime.Clock.System.now().toKtInstant()

        // Delete the property
        appConfigRepository.delete(applicationName, domain, propertyKey)

        // Create audit record for deletion
        auditRepository.insert(
            AppConfigAudit(
                applicationName = applicationName,
                domain = domain,
                propertyKey = propertyKey,
                oldPropertyValue = existing.propertyValue,
                newPropertyValue = null,
                operation = AuditOperation.DELETED,
                updatedBy = "system",
                updatedTm = now,
                versionNumber = currentSync.versionNumber + 1
            )
        )

        // Increment version
        configSyncRepository.incrementVersion(applicationName, domain, "system")

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

    @OptIn(ExperimentalTime::class)
    override fun onboardService(
        domain: String,
        applicationName: String,
        file: io.javalin.http.UploadedFile
    ): PropertiesWithSyncResponse {
        // Check if service already exists (domain + application combination)
        val existingProperties = appConfigRepository.findByAppAndDomain(applicationName, domain)
        if (existingProperties.isNotEmpty()) {
            throw IllegalArgumentException("Service '$applicationName' is already onboarded in domain '$domain'. " +
                    "Each domain + application combination must be unique.")
        }

        // Check if ConfigSync already exists for this domain + application
        val existingSync = configSyncRepository.findByAppAndDomain(applicationName, domain)
        if (existingSync != null) {
            throw IllegalArgumentException("Configuration already exists for service '$applicationName' in domain '$domain'")
        }

        try {
            // Read and parse the file
            val fileContent = String(file.content().readAllBytes(), Charsets.UTF_8)
            val fileName = file.filename().lowercase()

            val now = kotlinx.datetime.Clock.System.now().toKtInstant()
            val properties = parseConfigurationFile(fileContent, fileName)

            if (properties.isEmpty()) {
                throw IllegalArgumentException("Configuration file is empty or has no valid properties")
            }

            // Insert all properties
            properties.forEach { (key, value) ->
                val appConfig = AppConfig(
                    applicationName = applicationName,
                    domain = domain,
                    propertyKey = key,
                    propertyValue = value,
                    createdBy = "system",
                    createdTm = now,
                    updatedBy = "system",
                    updatedTm = now
                )
                appConfigRepository.insert(appConfig)
            }

            // Create ConfigSync entry
            configSyncRepository.insert(
                ConfigSync(
                    applicationName = applicationName,
                    domain = domain,
                    versionNumber = 1,
                    updatedBy = "system",
                    updatedTm = now
                )
            )

            // Create audit records for each property
            properties.forEach { (key, value) ->
                auditRepository.insert(
                    AppConfigAudit(
                        applicationName = applicationName,
                        domain = domain,
                        propertyKey = key,
                        oldPropertyValue = null,
                        newPropertyValue = value,
                        operation = AuditOperation.ADDED,
                        updatedBy = "system",
                        updatedTm = now,
                        versionNumber = 1
                    )
                )
            }

            // Return the newly onboarded service configuration
            return getPropertiesByAppAndDomain(applicationName, domain)

        } catch (e: Exception) {
            if (e.message?.contains("already onboarded") == true ||
                e.message?.contains("already exists") == true) {
                throw e
            }
            throw Exception("Failed to parse configuration file: ${e.message}", e)
        }
    }

    /**
     * Parse configuration file based on file type
     * Supports .properties and .yaml/.yml files
     */
    private fun parseConfigurationFile(content: String, fileName: String): Map<String, String> {
        return when {
            fileName.endsWith(".properties") -> parsePropertiesFile(content)
            fileName.endsWith(".yaml") || fileName.endsWith(".yml") -> parseYamlFile(content)
            fileName.endsWith(".json") -> parseJsonFile(content)
            else -> throw IllegalArgumentException("Unsupported file type. Supported: .properties, .yaml, .yml, .json")
        }
    }

    /**
     * Parse .properties file format
     * Supports key=value format, ignores comments and empty lines
     */
    private fun parsePropertiesFile(content: String): Map<String, String> {
        val properties = mutableMapOf<String, String>()
        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("!")) {
                val parts = trimmed.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    if (key.isNotEmpty()) {
                        properties[key] = value
                    }
                }
            }
        }
        return properties
    }

    /**
     * Parse YAML file format with support for nested structures
     * Flattens nested keys to dot notation
     * Example: spring.data.mongodb.uri becomes "spring.data.mongodb.uri"
     */
    private fun parseYamlFile(content: String): Map<String, String> {
        val properties = mutableMapOf<String, String>()
        val lines = content.lines()
        val stack = mutableListOf<String>() // Stack to track nesting levels

        lines.forEach { line ->
            if (line.isBlank() || line.trim().startsWith("#")) {
                return@forEach
            }

            // Calculate indentation level (number of spaces at start)
            val indentation = line.takeWhile { it == ' ' }.length
            val trimmed = line.trim()

            // Remove previous keys from stack that are deeper than current indentation
            while (stack.isNotEmpty() && stack.size > indentation / 2) {
                stack.removeAt(stack.size - 1)
            }

            // Parse key: value
            if (trimmed.contains(":")) {
                val parts = trimmed.split(":", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim().removeSurrounding("\"").removeSurrounding("'")

                    if (key.isNotEmpty()) {
                        // Build full key path with dot notation
                        val fullKey = if (value.isEmpty()) {
                            // This is a parent key (no value), add to stack
                            stack.add(key)
                            null
                        } else {
                            // This is a leaf key with value
                            stack.add(key)
                            val result = stack.joinToString(".")
                            stack.removeAt(stack.size - 1)
                            result
                        }

                        if (fullKey != null && value.isNotEmpty()) {
                            properties[fullKey] = value
                        }
                    }
                }
            }
        }

        return properties
    }

    /**
     * Parse JSON file format with support for nested structures
     * Flattens nested objects to dot notation
     * Example: { "spring": { "application": { "name": "app" } } }
     * becomes "spring.application.name": "app"
     */
    private fun parseJsonFile(content: String): Map<String, String> {
        return try {
            val jsonNode = objectMapper.readTree(content)
            val properties = mutableMapOf<String, String>()
            flattenJsonNode("", jsonNode, properties)
            properties
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid JSON format: ${e.message}")
        }
    }

    /**
     * Recursively flatten JSON node to dot notation
     */
    private fun flattenJsonNode(
        prefix: String,
        node: com.fasterxml.jackson.databind.JsonNode,
        properties: MutableMap<String, String>
    ) {
        when {
            node.isObject -> {
                // Recursively process object fields
                node.fields().forEach { (key, value) ->
                    val newKey = if (prefix.isEmpty()) key else "$prefix.$key"
                    flattenJsonNode(newKey, value, properties)
                }
            }
            node.isArray -> {
                // For arrays, we'll skip them (or could add [index] notation if needed)
                // For now, arrays are not processed
            }
            else -> {
                // Leaf node - add to properties
                if (prefix.isNotEmpty()) {
                    properties[prefix] = node.asText()
                }
            }
        }
    }
}

