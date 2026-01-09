package com.tk.learn.configserver.repository

import org.jdbi.v3.core.Jdbi
import com.tk.learn.domain.models.ConfigSync
import kotlin.time.Instant
import kotlin.time.ExperimentalTime
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Row mapper for ConfigSync
 */
class ConfigSyncRowMapper : RowMapper<ConfigSync> {
    @OptIn(ExperimentalTime::class)
    @Throws(SQLException::class)
    override fun map(rs: ResultSet, ctx: StatementContext): ConfigSync {
        return ConfigSync(
            id = rs.getLong("id"),
            applicationName = rs.getString("application_name"),
            domain = rs.getString("domain"),
            versionNumber = rs.getInt("version_number"),
            updatedBy = rs.getString("updated_by"),
            updatedTm = Instant.fromEpochMilliseconds(rs.getTimestamp("updated_tm").time)
        )
    }
}

/**
 * Interface for ConfigSync repository operations
 */
interface ConfigSyncRepository {
    fun findByAppAndDomain(applicationName: String, domain: String): ConfigSync?
    fun insert(configSync: ConfigSync): Long
    fun update(configSync: ConfigSync): Int
    fun incrementVersion(applicationName: String, domain: String, updatedBy: String): Int
}

/**
 * Implementation of ConfigSyncRepository
 */
class ConfigSyncRepositoryImpl(private val jdbi: Jdbi) : ConfigSyncRepository {

    @OptIn(ExperimentalTime::class)
    override fun findByAppAndDomain(applicationName: String, domain: String): ConfigSync? {
        return jdbi.withHandle<ConfigSync?, Exception> { handle ->
            handle.createQuery(
                """
                SELECT id, application_name, domain, version_number, updated_by, updated_tm
                FROM config_sync
                WHERE application_name = :applicationName AND domain = :domain
                """
            )
                .bind("applicationName", applicationName)
                .bind("domain", domain)
                .map(ConfigSyncRowMapper())
                .findOne()
                .orElse(null)
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun insert(configSync: ConfigSync): Long {
        return jdbi.withHandle<Long, Exception> { handle ->
            handle.createUpdate(
                """
                INSERT INTO config_sync (application_name, domain, version_number, updated_by, updated_tm)
                VALUES (:applicationName, :domain, :versionNumber, :updatedBy, :updatedTm)
                """
            )
                .bind("applicationName", configSync.applicationName)
                .bind("domain", configSync.domain)
                .bind("versionNumber", configSync.versionNumber)
                .bind("updatedBy", configSync.updatedBy)
                .bind("updatedTm", java.sql.Timestamp(configSync.updatedTm.toEpochMilliseconds()))
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long::class.java)
                .one()
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun update(configSync: ConfigSync): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate(
                """
                UPDATE config_sync
                SET version_number = :versionNumber, updated_by = :updatedBy, updated_tm = :updatedTm
                WHERE application_name = :applicationName AND domain = :domain
                """
            )
                .bind("versionNumber", configSync.versionNumber)
                .bind("updatedBy", configSync.updatedBy)
                .bind("updatedTm", java.sql.Timestamp(configSync.updatedTm.toEpochMilliseconds()))
                .bind("applicationName", configSync.applicationName)
                .bind("domain", configSync.domain)
                .execute()
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun incrementVersion(applicationName: String, domain: String, updatedBy: String): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate(
                """
                UPDATE config_sync
                SET version_number = version_number + 1, updated_by = :updatedBy, updated_tm = :updatedTm
                WHERE application_name = :applicationName AND domain = :domain
                """
            )
                .bind("updatedBy", updatedBy)
                .bind("updatedTm", java.sql.Timestamp(System.currentTimeMillis()))
                .bind("applicationName", applicationName)
                .bind("domain", domain)
                .execute()
        }
    }
}

