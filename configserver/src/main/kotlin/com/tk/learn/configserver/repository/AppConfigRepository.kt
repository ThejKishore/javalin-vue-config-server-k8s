package com.tk.learn.configserver.repository

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.StatementContext
import com.tk.learn.domain.models.AppConfig
import kotlin.time.Instant
import kotlin.time.ExperimentalTime
import org.jdbi.v3.core.mapper.RowMapper
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Row mapper for AppConfig
 */
class AppConfigRowMapper : RowMapper<AppConfig> {
    @OptIn(ExperimentalTime::class)
    @Throws(SQLException::class)
    override fun map(rs: ResultSet, ctx: StatementContext): AppConfig {
        return AppConfig(
            id = rs.getLong("id"),
            applicationName = rs.getString("application_name"),
            domain = rs.getString("domain"),
            propertyKey = rs.getString("property_key"),
            propertyValue = rs.getString("property_value"),
            createdBy = rs.getString("created_by"),
            createdTm = Instant.fromEpochMilliseconds(rs.getTimestamp("created_tm").time),
            updatedBy = rs.getString("updated_by"),
            updatedTm = Instant.fromEpochMilliseconds(rs.getTimestamp("updated_tm").time)
        )
    }
}

/**
 * Interface for AppConfig repository operations
 */
interface AppConfigRepository {
    fun findByAppAndDomain(applicationName: String, domain: String): List<AppConfig>
    fun findByAppDomainAndKey(applicationName: String, domain: String, propertyKey: String): AppConfig?
    fun insert(appConfig: AppConfig): Long
    fun update(appConfig: AppConfig): Int
    fun delete(applicationName: String, domain: String, propertyKey: String): Int
    fun deleteByAppAndDomain(applicationName: String, domain: String): Int
    fun getDistinctDomains(): List<String>
    fun getApplicationsByDomain(domain: String): List<String>
}

/**
 * Implementation of AppConfigRepository
 */
class AppConfigRepositoryImpl(private val jdbi: Jdbi) : AppConfigRepository {

    @OptIn(ExperimentalTime::class)
    override fun findByAppAndDomain(applicationName: String, domain: String): List<AppConfig> {
        return jdbi.withHandle<List<AppConfig>, Exception> { handle ->
            handle.createQuery(
                """
                SELECT id, application_name, domain, property_key, property_value,
                       created_by, created_tm, updated_by, updated_tm
                FROM app_config
                WHERE application_name = :applicationName AND domain = :domain
                ORDER BY property_key
                """
            )
                .bind("applicationName", applicationName)
                .bind("domain", domain)
                .map(AppConfigRowMapper())
                .list()
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun findByAppDomainAndKey(
        applicationName: String,
        domain: String,
        propertyKey: String
    ): AppConfig? {
        return jdbi.withHandle<AppConfig?, Exception> { handle ->
            handle.createQuery(
                """
                SELECT id, application_name, domain, property_key, property_value,
                       created_by, created_tm, updated_by, updated_tm
                FROM app_config
                WHERE application_name = :applicationName AND domain = :domain AND property_key = :propertyKey
                """
            )
                .bind("applicationName", applicationName)
                .bind("domain", domain)
                .bind("propertyKey", propertyKey)
                .map(AppConfigRowMapper())
                .findOne()
                .orElse(null)
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun insert(appConfig: AppConfig): Long {
        return jdbi.withHandle<Long, Exception> { handle ->
            val result = handle.createUpdate(
                """
                INSERT INTO app_config 
                (application_name, domain, property_key, property_value, created_by, created_tm, updated_by, updated_tm)
                VALUES (:applicationName, :domain, :propertyKey, :propertyValue, :createdBy, :createdTm, :updatedBy, :updatedTm)
                """
            )
                .bind("applicationName", appConfig.applicationName)
                .bind("domain", appConfig.domain)
                .bind("propertyKey", appConfig.propertyKey)
                .bind("propertyValue", appConfig.propertyValue)
                .bind("createdBy", appConfig.createdBy)
                .bind("createdTm", java.sql.Timestamp(appConfig.createdTm.toEpochMilliseconds()))
                .bind("updatedBy", appConfig.updatedBy)
                .bind("updatedTm", java.sql.Timestamp(appConfig.updatedTm.toEpochMilliseconds()))
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long::class.java)
                .one()
            result
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun update(appConfig: AppConfig): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate(
                """
                UPDATE app_config
                SET property_value = :propertyValue, updated_by = :updatedBy, updated_tm = :updatedTm
                WHERE application_name = :applicationName AND domain = :domain AND property_key = :propertyKey
                """
            )
                .bind("propertyValue", appConfig.propertyValue)
                .bind("updatedBy", appConfig.updatedBy)
                .bind("updatedTm", java.sql.Timestamp(appConfig.updatedTm.toEpochMilliseconds()))
                .bind("applicationName", appConfig.applicationName)
                .bind("domain", appConfig.domain)
                .bind("propertyKey", appConfig.propertyKey)
                .execute()
        }
    }

    override fun delete(applicationName: String, domain: String, propertyKey: String): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate(
                """
                DELETE FROM app_config
                WHERE application_name = :applicationName AND domain = :domain AND property_key = :propertyKey
                """
            )
                .bind("applicationName", applicationName)
                .bind("domain", domain)
                .bind("propertyKey", propertyKey)
                .execute()
        }
    }

    override fun deleteByAppAndDomain(applicationName: String, domain: String): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate(
                """
                DELETE FROM app_config
                WHERE application_name = :applicationName AND domain = :domain
                """
            )
                .bind("applicationName", applicationName)
                .bind("domain", domain)
                .execute()
        }
    }

    override fun getDistinctDomains(): List<String> {
        return jdbi.withHandle<List<String>, Exception> { handle ->
            handle.createQuery(
                """
                SELECT DISTINCT domain
                FROM app_config
                ORDER BY domain
                """
            )
                .mapTo(String::class.java)
                .list()
        }
    }

    override fun getApplicationsByDomain(domain: String): List<String> {
        return jdbi.withHandle<List<String>, Exception> { handle ->
            handle.createQuery(
                """
                SELECT DISTINCT application_name
                FROM app_config
                WHERE domain = :domain
                ORDER BY application_name
                """
            )
                .bind("domain", domain)
                .mapTo(String::class.java)
                .list()
        }
    }
}

