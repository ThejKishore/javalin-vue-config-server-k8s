package com.tk.learn.configserver.repository

import org.jdbi.v3.core.Jdbi
import com.tk.learn.domain.models.AppConfigAudit
import com.tk.learn.domain.models.AuditOperation
import kotlin.time.Instant
import kotlin.time.ExperimentalTime
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Row mapper for AppConfigAudit
 */
class AppConfigAuditRowMapper : RowMapper<AppConfigAudit> {
    @OptIn(ExperimentalTime::class)
    @Throws(SQLException::class)
    override fun map(rs: ResultSet, ctx: StatementContext): AppConfigAudit {
        return AppConfigAudit(
            id = rs.getLong("id"),
            applicationName = rs.getString("application_name"),
            domain = rs.getString("domain"),
            propertyKey = rs.getString("property_key"),
            oldPropertyValue = rs.getString("old_property_value"),
            newPropertyValue = rs.getString("new_property_value"),
            operation = AuditOperation.valueOf(rs.getString("operation")),
            updatedBy = rs.getString("updated_by"),
            updatedTm = Instant.fromEpochMilliseconds(rs.getTimestamp("updated_tm").time),
            versionNumber = rs.getInt("version_number")
        )
    }
}

/**
 * Interface for AppConfigAudit repository operations
 */
interface AppConfigAuditRepository {
    fun findByAppAndDomain(applicationName: String, domain: String, limit: Int = 100): List<AppConfigAudit>
    fun insert(audit: AppConfigAudit): Long
}

/**
 * Implementation of AppConfigAuditRepository
 */
class AppConfigAuditRepositoryImpl(private val jdbi: Jdbi) : AppConfigAuditRepository {

    override fun findByAppAndDomain(
        applicationName: String,
        domain: String,
        limit: Int
    ): List<AppConfigAudit> {
        return jdbi.withHandle<List<AppConfigAudit>, Exception> { handle ->
            handle.createQuery(
                """
                SELECT id, application_name, domain, property_key, old_property_value, new_property_value,
                       operation, updated_by, updated_tm, version_number
                FROM app_config_audit
                WHERE application_name = :applicationName AND domain = :domain
                ORDER BY version_number DESC, updated_tm DESC
                LIMIT :limit
                """
            )
                .bind("applicationName", applicationName)
                .bind("domain", domain)
                .bind("limit", limit)
                .map(AppConfigAuditRowMapper())
                .list()
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun insert(audit: AppConfigAudit): Long {
        return jdbi.withHandle<Long, Exception> { handle ->
            handle.createUpdate(
                """
                INSERT INTO app_config_audit 
                (application_name, domain, property_key, old_property_value, new_property_value, operation, updated_by, updated_tm, version_number)
                VALUES (:applicationName, :domain, :propertyKey, :oldPropertyValue, :newPropertyValue, :operation, :updatedBy, :updatedTm, :versionNumber)
                """
            )
                .bind("applicationName", audit.applicationName)
                .bind("domain", audit.domain)
                .bind("propertyKey", audit.propertyKey)
                .bind("oldPropertyValue", audit.oldPropertyValue)
                .bind("newPropertyValue", audit.newPropertyValue)
                .bind("operation", audit.operation.name)
                .bind("updatedBy", audit.updatedBy)
                .bind("updatedTm", java.sql.Timestamp(audit.updatedTm.toEpochMilliseconds()))
                .bind("versionNumber", audit.versionNumber)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long::class.java)
                .one()
        }
    }
}

