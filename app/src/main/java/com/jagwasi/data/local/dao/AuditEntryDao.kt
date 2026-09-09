package com.jagwasi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jagwasi.data.local.entity.AuditEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditEntryDao {
    @Insert
    suspend fun insert(auditEntry: AuditEntry): Long

    @Query("SELECT * FROM audit_entries ORDER BY timestamp DESC")
    fun getAllAuditEntries(): Flow<List<AuditEntry>>

    @Query("SELECT * FROM audit_entries WHERE actor = :actor ORDER BY timestamp DESC")
    fun getAuditEntriesByActor(actor: String): Flow<List<AuditEntry>>

    @Query("SELECT * FROM audit_entries WHERE action = :action ORDER BY timestamp DESC")
    fun getAuditEntriesByAction(action: String): Flow<List<AuditEntry>>

    @Query("SELECT * FROM audit_entries WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getAuditEntriesBetween(start: Long, end: Long): Flow<List<AuditEntry>>
}
